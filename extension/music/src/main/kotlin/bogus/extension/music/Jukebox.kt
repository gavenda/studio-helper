package bogus.extension.music

import bogus.extension.music.db.guilds
import bogus.util.escapedBackticks
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.mapToTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Plays music via a play request.
 */
object Jukebox : KoinComponent {
    private val log = KotlinLogging.logger {}
    private val players = ConcurrentHashMap<Snowflake, GuildMusicPlayer>()
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
                val gmp = GuildMusicPlayer(guildId)
                log.debug { """msg="Creating guild music player" guildId=$guildId""" }
                return@computeIfAbsent gmp
            }
        }
    }

    /**
     * Returns a player for the specified guild snowflake.
     * @param guildId the guild snowflake to retrieve a player from
     */
    fun playerFor(guildId: Snowflake): GuildMusicPlayer {
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

                bind(textChannel)

                log.info { """msg="Guild bound" guild=${guild.id}""" }
            }

            // Apply volume
            volumeTo(dbGuild.volume)
        }
    }

    suspend fun tryToDisconnect() {
        players.forEach { (guildId, player) ->
            val lastPlayMillis = player.lastPlayMillis.get()
            if (!player.playing && lastPlayMillis < System.currentTimeMillis()) {
                val link = Lava.linkFor(guildId)
                if (link.state == Link.State.CONNECTED) {
                    link.disconnectAudio()
                }
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
        val identifiers: List<String>,
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
        val (respond, identifiers, guild, mention, userId, locale) = request

        if (identifiers.isEmpty()) {
            log.info { """msg="No identifiers found", identifiers="${request.identifiers}" user=$userId guild=${guild.id}""" }

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.link.loadItem(identifier)

        val trackLoaded: suspend (Track) -> Unit = { track ->
            track.meta = AudioTrackMeta(mention, userId)

            if (guild.player.playing) {
                val currentTrack = guild.player.playingTrack
                if (currentTrack != null) {
                    guild.player.addFirst(currentTrack)
                }
                guild.player.addFirst(track)
                guild.player.skip()
            } else {
                guild.player.addFirst(track, update = true)
            }

            respond(
                tp.translate(
                    key = "jukebox.response.play-now",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            )
        }

        when (item.loadType) {
            TrackResponse.LoadType.TRACK_LOADED -> {
                trackLoaded(item.track.toTrack())
            }
            TrackResponse.LoadType.PLAYLIST_LOADED -> {
                respond(tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE))
            }
            TrackResponse.LoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first().toTrack())
            }
            TrackResponse.LoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackResponse.LoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.exception)
                    )
                )
            }
        }

        log.info { """msg="Playing now" identifier="$identifier" user=$userId guild=${guild.id}""" }

        return ""
    }

    /**
     * Plays a play request next in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playNext(request: PlayRequest): String = mutex.withLock {
        val (respond, identifiers, guild, mention, userId, locale) = request

        if (identifiers.isEmpty()) {
            log.info { """msg="No identifiers found" identifiers="${request.identifiers}" user=$userId guild=${guild.id}""" }

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.link.loadItem(identifier)

        val trackLoaded: suspend (Track) -> Unit = { track ->
            track.meta = AudioTrackMeta(mention, userId)

            val started = guild.player.addFirst(track)

            if (started) {
                respond(
                    tp.translate(
                        key = "jukebox.response.play-now",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(track.title.escapedBackticks)
                    )
                )
            } else {
                respond(
                    tp.translate(
                        key = "jukebox.response.play-next",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(track.title.escapedBackticks)
                    )
                )
            }
        }

        when (item.loadType) {
            TrackResponse.LoadType.TRACK_LOADED -> {
                trackLoaded(item.track.toTrack())
            }
            TrackResponse.LoadType.PLAYLIST_LOADED -> {
                respond(tp.translate("jukebox.response.no-cheating", locale, TRANSLATION_BUNDLE))
            }
            TrackResponse.LoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first().toTrack())
            }
            TrackResponse.LoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackResponse.LoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.exception)
                    )
                )
            }
        }

        log.info { """msg="Playing next" identifier="$identifier" user=$userId guild=${guild.id}""" }

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
        val trackLoaded: suspend (Track) -> Unit = { track ->
            track.meta = AudioTrackMeta(mention, userId)
            guild.player.add(track, update = true)
        }
        val item = guild.link.loadItem(identifier)

        when (item.loadType) {
            TrackResponse.LoadType.TRACK_LOADED -> {
                trackLoaded(item.track.toTrack())
            }
            TrackResponse.LoadType.PLAYLIST_LOADED -> {
                val tracks = item.tracks.mapToTrack()

                tracks.forEach {
                    it.meta = AudioTrackMeta(mention, userId)
                }

                guild.player.add(*tracks.toTypedArray(), update = true)
            }
            TrackResponse.LoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first().toTrack())
            }
            TrackResponse.LoadType.NO_MATCHES -> {
                log.debug { """msg=Nothing found""" }
            }
            TrackResponse.LoadType.LOAD_FAILED -> {
                log.error { """msg=Nothing found""" }
            }
        }
    }

    /**
     * Plays a play request later in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playLater(request: PlayRequest): String = mutex.withLock {
        val (respond, identifiers, guild, mention, userId, locale) = request
        val link = Lava.linkFor(guild)

        if (identifiers.isEmpty()) {
            log.info { """msg="No identifiers found" identifiers="${request.identifiers}" user=$userId guild=${guild.id}""" }

            return tp.translate("jukebox.response.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            identifiers.forEach { identifier ->
                // Fire and forget swoosh!
                CoroutineScope(Dispatchers.IO).launch {
                    playLaterSilently(
                        identifier = identifier,
                        guild = guild,
                        mention = mention,
                        userId = userId
                    )
                }
            }

            return tp.translate(
                key = "jukebox.response.playlist-unknown-later",
                bundleName = TRANSLATION_BUNDLE,
                locale,
                replacements = arrayOf(identifiers.size)
            )
        }

        val identifier = identifiers.first()
        val item = link.loadItem(identifier)
        val trackLoaded: suspend (Track) -> Unit = { track ->
            track.meta = AudioTrackMeta(mention, userId)

            val started = guild.player.add(track, update = true)

            if (started) {
                respond(
                    tp.translate(
                        key = "jukebox.response.play-now",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(track.title.escapedBackticks)
                    )
                )
            } else {
                respond(
                    tp.translate(
                        key = "jukebox.response.play-later",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(track.title.escapedBackticks)
                    )
                )
            }
        }

        when (item.loadType) {
            TrackResponse.LoadType.TRACK_LOADED -> {
                trackLoaded(item.track.toTrack())
            }
            TrackResponse.LoadType.PLAYLIST_LOADED -> {
                val tracks = item.tracks.mapToTrack()

                tracks.forEach {
                    it.meta = AudioTrackMeta(mention, userId)
                }

                val started = guild.player.add(*tracks.toTypedArray(), update = true)
                val playlistName = item.playlistInfo.name?.escapedBackticks

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
            TrackResponse.LoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first().toTrack())
            }
            TrackResponse.LoadType.NO_MATCHES -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackResponse.LoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "jukebox.response.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.exception)
                    )
                )
            }
        }

        log.info { """msg="Playing later" identifier="$identifier" user=$userId guild=${guild.id}""" }

        return ""
    }
}