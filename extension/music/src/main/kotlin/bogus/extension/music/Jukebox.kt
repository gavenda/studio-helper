package bogus.extension.music

import bogus.extension.music.db.guilds
import bogus.extension.music.player.*

import bogus.util.escapedBackticks
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import io.micrometer.core.instrument.MeterRegistry
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
    private val log = KotlinLogging.logger {}
    private val players = ConcurrentHashMap<Snowflake, MusicPlayer>()
    private val tp by inject<TranslationsProvider>()
    private val db by inject<Database>()
    private val registry by inject<MeterRegistry>()

    init {
        registry.gauge(Metric.SONGS_PLAYING, players.values) {
            it.map { player -> player.queued }.reduce { acc, queued -> acc + queued }.toDouble()
        }
    }

    /**
     * Register a guild to this [Jukebox].
     * @param guildId the guild snowflake to register
     */
    fun register(guildId: Snowflake) {
        players.computeIfAbsent(guildId) {
            val musicPlayer = if (LAVAKORD_ENABLED) {
                LinkMusicPlayer(guildId)
            } else {
                LavaMusicPlayer(guildId)
            }
            log.debug { "Music player created" }
            return@computeIfAbsent musicPlayer
        }
    }

    /**
     * Returns a player for the specified guild snowflake.
     * @param guildId the guild snowflake to retrieve a player from
     */
    fun playerFor(guildId: Snowflake): MusicPlayer {
        return players[guildId] ?: error("Guild not registered")
    }

    private fun mutexFor(request: PlayRequest): Mutex {
        return playerFor(request.guild.id).mutex
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

                log.info { "Guild bound" }
            }
        }
    }

    suspend fun tryToDisconnect() {
        players.values.forEach { player ->
            val lastPlayMillis = player.lastPlayMillis
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
    suspend fun playNow(request: PlayRequest): String = mutexFor(request).withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info { "No identifiers found" }

            return tp.translate("response.jukebox.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("response.jukebox.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.player.loader.loadItem(identifier)

        val trackLoaded: suspend (MusicTrack) -> String = {
            val track = it.copy(userId = userId, mention = mention)
            if (guild.player.playing) {
                val currentTrack = guild.player.playingTrack
                if (currentTrack != null) {
                    guild.player.addFirst(currentTrack.makeClone())
                }
                guild.player.addFirst(track)
                guild.player.skip()
            } else {
                guild.player.addFirst(track, update = true)
            }

            registry.counter(Metric.SONGS_QUEUED).increment()

            tp.translate(
                key = "response.jukebox.play-now",
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
                respond(tp.translate("response.jukebox.no-cheating", locale, TRANSLATION_BUNDLE))
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
                        key = "response.jukebox.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "response.jukebox.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info { "Playing now" }

        return ""
    }

    /**
     * Plays a play request next in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playNext(request: PlayRequest): String = mutexFor(request).withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info { "No identifiers found" }

            return tp.translate("response.jukebox.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            return tp.translate("response.jukebox.no-cheating", locale, TRANSLATION_BUNDLE)
        }

        val identifier = identifiers.first()
        val item = guild.player.loader.loadItem(identifier)

        val trackLoaded: suspend (MusicTrack) -> String = {
            val track = it.copy(userId = userId, mention = mention)
            val started = guild.player.addFirst(track, update = true)

            // Update metrics
            registry.counter(Metric.SONGS_QUEUED).increment()

            if (started) {
                tp.translate(
                    key = "response.jukebox.play-now",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            } else {
                tp.translate(
                    key = "response.jukebox.play-next",
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
                respond(tp.translate("response.jukebox.no-cheating", locale, TRANSLATION_BUNDLE))
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
                        key = "response.jukebox.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "response.jukebox.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info { "Playing next" }

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

            // Update metrics
            registry.counter(Metric.SONGS_QUEUED).increment()
        }
        val item = guild.player.loader.loadItem(identifier)

        when (item.loadType) {
            TrackLoadType.TRACK_LOADED -> {
                trackLoaded(item.track)
            }
            TrackLoadType.PLAYLIST_LOADED -> {
                // Update metrics
                registry.counter(Metric.SONGS_QUEUED).increment(item.tracks.size.toDouble())
                guild.player.add(*item.tracks.toTypedArray(), update = true)
            }
            TrackLoadType.SEARCH_RESULT -> {
                trackLoaded(item.tracks.first())
            }
            TrackLoadType.NO_MATCHES -> {
                log.debug { "Nothing found" }
            }
            TrackLoadType.LOAD_FAILED -> {
                log.debug { "Nothing found" }
            }
        }
    }

    /**
     * Plays a play request later in the queue.
     * @return response, empty string if successful.
     */
    suspend fun playLater(request: PlayRequest): String = mutexFor(request).withLock {
        val (respond, respondMultiple, parseResult, guild, mention, userId, locale) = request
        val identifiers = parseResult.identifiers

        if (identifiers.isEmpty()) {
            log.info { "No identifiers found" }

            return tp.translate("response.jukebox.not-found", locale, TRANSLATION_BUNDLE)
        } else if (identifiers.size > 1) {
            CoroutineScope(Dispatchers.IO).launch {
                identifiers.forEach { identifier ->
                    // Maintain order
                    mutexFor(request).withLock {
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
                key = "response.jukebox.playlist-unknown-later",
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

            // Update metrics
            registry.counter(Metric.SONGS_QUEUED)

            if (started) {
                tp.translate(
                    key = "response.jukebox.play-now",
                    bundleName = TRANSLATION_BUNDLE,
                    locale,
                    replacements = arrayOf(track.title.escapedBackticks)
                )
            } else {
                tp.translate(
                    key = "response.jukebox.play-later",
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

                // Update metrics
                registry.counter(Metric.SONGS_QUEUED).increment(item.tracks.size.toDouble())

                if (started) {
                    respond(
                        tp.translate(
                            key = "response.jukebox.playlist-now",
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
                            key = "response.jukebox.playlist-later",
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
                        key = "response.jukebox.no-matches",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(identifier)
                    )
                )
            }
            TrackLoadType.LOAD_FAILED -> {
                respond(
                    tp.translate(
                        key = "response.jukebox.error",
                        bundleName = TRANSLATION_BUNDLE,
                        locale,
                        replacements = arrayOf(item.error ?: "Loading failed")
                    )
                )
            }
        }

        log.info { "Playing later" }

        return ""
    }
}