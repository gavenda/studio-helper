package bogus.extension.music

import bogus.extension.music.db.guilds
import bogus.extension.music.player.*
import bogus.util.asLogFMT
import bogus.util.escapedBackticks
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Plays music via a play request.
 */
object Jukebox : KordExKoinComponent {
    private val log = KotlinLogging.logger {}.asLogFMT()
    private val players = ConcurrentHashMap<Snowflake, MusicPlayer>()
    private val mutex = Mutex()
    private val tp by inject<TranslationsProvider>()
    private val db by inject<Database>()

    /**
     * Register a guild to this [Jukebox].
     * @param guildId the guild snowflake to register
     */
    suspend fun register(guildId: Snowflake) {
        mutex.withLock {
            players.computeIfAbsent(guildId) {
                val musicPlayer = if (LAVAKORD_ENABLED) {
                    LinkMusicPlayer(guildId)
                } else {
                    LavaMusicPlayer(guildId)
                }
                log.debug(
                    msg = "Music player created",
                    context = mapOf(
                        "guildId" to guildId
                    )
                )
                return@computeIfAbsent musicPlayer
            }
        }
    }

    /**
     * Returns a player for the specified guild snowflake.
     * @param guildId the guild snowflake to retrieve a player from
     */
    fun playerFor(guildId: Snowflake): MusicPlayer {
        return players[guildId] ?: error("Guild not registered")
    }

    suspend fun bind(guild: Guild) {
        val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guild.idLong } ?: return
        val textChannelId = dbGuild.textChannelId
        val lastMessageId = dbGuild.lastMessageId

        playerFor(guild.id).apply {
            if (textChannelId != null) {
                val textChannel = guild.getChannel(Snowflake(textChannelId)) as GuildMessageChannel

                if (lastMessageId != null) {
                    val textMessage = textChannel.getMessage(Snowflake(lastMessageId))
                    textMessage.delete("Crash cleanup")
                }

                // Apply volume
                volumeTo(dbGuild.volume, update = false)

                dbGuild.lastMessageId = bind(textChannel)?.value?.toLong()
                dbGuild.flushChanges()

                log.info(
                    msg = "Guild bound",
                    context = mapOf(
                        "guildId" to guild.id
                    )
                )
            }
        }
    }

    suspend fun tryToDisconnect() {
        players.values.forEach { player ->
            val lastPlayMillis = player.lastPlayMillis.value
            if (!player.playing && System.currentTimeMillis() > lastPlayMillis) {
                player.disconnect()
            }
        }
    }

    suspend fun destroy() {
        players.forEach { (guildId, player) ->
            player.unbind()

            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guildId.value.toLong() } ?: return@forEach

            dbGuild.lastMessageId = null
            dbGuild.flushChanges()
        }
    }

    data class PlayRequest(
        val respond: suspend (String) -> Unit,
        val respondMultiple: suspend (List<MusicTrack>, suspend (MusicTrack) -> String) -> Unit,
        val parseResult: IdentifierParser.IdentifierParseResult,
        val guild: GuildBehavior,
        val mention: String,
        val userId: Snowflake,
        val locale: Locale
    )

    /**
     * Plays a play request immediately.
     * @return response, empty string if successful.
     */
    suspend fun playNow(request: PlayRequest): String = mutex.withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info(
                msg = "No identifiers found",
                context = mapOf(
                    "identifiers" to identifiers,
                    "userId" to userId,
                    "guildId" to guild.id
                )
            )

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.player.loader.loadItem(identifier)

        val trackLoaded: suspend (MusicTrack) -> String = {
            val track = it.copy(userId = userId, mention = mention)
            if (guild.player.playing) {
                val currentTrack = guild.player.findPlayingTrack()
                if (currentTrack != null) {
                    guild.player.addFirst(currentTrack.makeClone())
                }
                guild.player.addFirst(track)
                guild.player.skip()
            } else {
                guild.player.addFirst(track, update = true)
            }

            tp.translate(
                key = "jukebox.response.play-now",
                bundleName = TRANSLATION_BUNDLE,
                locale,
                replacements = arrayOf(track.title.escapedBackticks)
            )
        }

        when (item.loadType) {
            TrackLoadType.TRACK_LOADED -> {
                respond(trackLoaded(item.track))
            }
            TrackLoadType.PLAYLIST_LOADED -> {
                respond(tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE))
            }
            TrackLoadType.SEARCH_RESULT -> {
                if (parseResult.spotify) {
                    respond(trackLoaded(item.tracks.first()))
                } else {
                    respondMultiple(item.tracks) {
                        trackLoaded(it)
                    }
                }
            }
            TrackLoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info(
            msg = "Playing now",
            context = mapOf(
                "identifier" to identifier,
                "userId" to userId,
                "guildId" to guild.id
            )
        )

        return ""
    }

    /**
     * Plays a play request next in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playNext(request: PlayRequest): String = mutex.withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info(
                msg = "No identifiers found",
                context = mapOf(
                    "identifiers" to identifiers,
                    "userId" to userId,
                    "guildId" to guild.id
                )
            )

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.player.loader.loadItem(identifier)

        val trackLoaded: suspend (MusicTrack) -> String = {
            val track = it.copy(userId = userId, mention = mention)
            val started = guild.player.addFirst(track, update = true)

            if (started) {
                tp.translate(
                    key = "jukebox.response.play-now",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            } else {
                tp.translate(
                    key = "jukebox.response.play-next",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            }
        }

        when (item.loadType) {
            TrackLoadType.TRACK_LOADED -> {
                respond(trackLoaded(item.track))
            }
            TrackLoadType.PLAYLIST_LOADED -> {
                respond(tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE))
            }
            TrackLoadType.SEARCH_RESULT -> {
                if (parseResult.spotify) {
                    respond(trackLoaded(item.tracks.first()))
                } else {
                    respondMultiple(item.tracks) {
                        trackLoaded(it)
                    }
                }
            }
            TrackLoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info(
            msg = "Playing next",
            context = mapOf(
                "identifier" to identifier,
                "userId" to userId,
                "guildId" to guild.id
            )
        )

        return ""
    }

    /**
     * Plays later and does a fire and forget operation. Will not wait for results to return.
     */
    suspend fun playLaterSilently(
        identifier: String,
        guild: GuildBehavior,
        mention: String,
        userId: Snowflake
    ) {
        val trackLoaded: suspend (MusicTrack) -> Unit = {
            val track = it.copy(userId = userId, mention = mention)
            guild.player.add(track, update = true, play = false)
        }
        val item = guild.player.loader.loadItem(identifier)

        when (item.loadType) {
            TrackLoadType.TRACK_LOADED -> {
                trackLoaded(item.track)
            }
            TrackLoadType.PLAYLIST_LOADED -> {
                guild.player.add(*item.tracks.toTypedArray(), update = true)
            }
            TrackLoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first())
            }
            TrackLoadType.NO_MATCHES -> {
                log.debug("Nothing found")
            }
            TrackLoadType.LOAD_FAILED -> {
                log.debug("Nothing found")
            }
        }
    }

    /**
     * Plays a play request later in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playLater(request: PlayRequest): String = mutex.withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info(
                msg = "No identifiers found",
                context = mapOf(
                    "identifiers" to identifiers,
                    "userId" to userId,
                    "guildId" to guild.id
                )
            )

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            CoroutineScope(Dispatchers.IO).launch {
                identifiers.forEach { identifier ->
                    // Maintain order
                    mutex.withLock {
                        playLaterSilently(
                            identifier = identifier,
                            guild = guild,
                            mention = mention,
                            userId = userId
                        )
                    }
                }
            }

            delay(2000)
            guild.player.attemptToPlay()

            return tp.translate(
                key = "jukebox.response.playlist-unknown-later",
                bundleName = TRANSLATION_BUNDLE,
                locale,
                replacements = arrayOf(identifiers.size)
            )
        }

        val identifier = identifiers.first()
        val item = guild.player.loader.loadItem(identifier)
        val trackLoaded: suspend (MusicTrack) -> String = {
            val track = it.copy(userId = userId, mention = mention)
            val started = guild.player.add(track, update = true)

            if (started) {
                tp.translate(
                    key = "jukebox.response.play-now",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            } else {
                tp.translate(
                    key = "jukebox.response.play-later",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            }
        }

        when (item.loadType) {
            TrackLoadType.TRACK_LOADED -> {
                respond(trackLoaded(item.track))
            }
            TrackLoadType.PLAYLIST_LOADED -> {
                val tracks = item.tracks
                    .map { it.copy(userId = userId, mention = mention) }
                val started = guild.player.add(*tracks.toTypedArray(), update = true)
                val playlistName = item.playlistInfo.name.escapedBackticks

                if (started) {
                    respond(
                        tp.translate(
                            key = "jukebox.response.playlist-now",
                            bundleName = TRANSLATION_BUNDLE,
                            locale,
                            replacements = arrayOf(
                                tracks.size,
                                playlistName
                            )
                        )
                    )
                } else {
                    respond(
                        tp.translate(
                            key = "jukebox.response.playlist-later",
                            bundleName = TRANSLATION_BUNDLE,
                            locale,
                            replacements = arrayOf(
                                tracks.size,
                                playlistName
                            )
                        )
                    )
                }
            }
            TrackLoadType.SEARCH_RESULT -> {
                if (parseResult.spotify) {
                    respond(trackLoaded(item.tracks.first()))
                } else {
                    val tracks = item.tracks
                        .map { it.copy(userId = userId, mention = mention) }

                    respondMultiple(tracks) {
                        trackLoaded(it)
                    }
                }
            }
            TrackLoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info(
            msg = "Playing later",
            context = mapOf(
                "identifier" to identifier,
                "userId" to userId,
                "guildId" to guild.id
            )
        )

        return ""
    }
}