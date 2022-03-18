package bogus.extension.music

import bogus.constants.ITEMS_PER_CHUNK
import bogus.extension.music.paginator.MessageQueuePaginator
import bogus.extension.music.paginator.MutablePages
import bogus.extension.music.paginator.messageQueuePaginator
import bogus.util.*
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicLong

/**
 * The guild's music player.
 */
class GuildMusicPlayer(guildId: Snowflake) : KoinComponent {
    private val tp by inject<TranslationsProvider>()
    private val queue = LinkedBlockingDeque<Track>()
    private val log = KotlinLogging.logger {}
    private val link = Lava.linkFor(guildId)
    private val player = link.player
    private val trackAttempts = ConcurrentHashMap<String, Int>()
    private val emptyQueueMessage = {
        tp.translate(
            key = "player.queue.message",
            bundleName = TRANSLATION_BUNDLE
        )
    }

    /**
     * Tune music player effects.
     */
    val effects = MusicEffects(player)

    /**
     * Last played song in milliseconds.
     */
    val lastPlayMillis = AtomicLong(System.currentTimeMillis())

    /**
     * The track request list of this player.
     * @return the track list
     */
    val tracks: List<Track> get() = queue.toList()

    /**
     * Returns the currently playing track.
     */
    val playingTrack: Track? get() = player.playingTrack

    /**
     * Returns true if the player is currently playing something regardless if it is paused.
     */
    val playing: Boolean
        get() = playingTrack != null

    /**
     * Returns true if the player is bound to a text channel.
     */
    val bound: Boolean
        get() = boundPaginator != null

    /**
     * The bound text channel id.
     */
    private var boundPaginator: MessageQueuePaginator? = null

    /**
     * The remaining duration of this player in milliseconds.
     */
    private val remainingDuration: Long
        get() {
            val playingTrack = player.playingTrack
            var duration = 0L
            if (playingTrack != null) {
                if (playing && playingTrack.isStream.not()) {
                    duration = playingTrack.length.inWholeMilliseconds - playingTrack.position.inWholeMilliseconds
                }
            }

            return duration + queue.duration
        }

    /**
     * The total duration of this player in milliseconds.
     */
    private val totalDuration: Long
        get() {
            val playingTrack = player.playingTrack
            var duration = 0L
            if (playingTrack != null) {
                if (playing && playingTrack.isStream.not()) {
                    duration = playingTrack.length.inWholeMilliseconds
                }
            }
            return duration + queue.duration
        }

    /**
     * Returns true if the player is paused.
     */
    val paused: Boolean
        get() = player.paused

    private var _looped = false

    /**
     * If this player is looping the current song.
     */
    val looped get() = _looped

    private var _loopedAll = false

    /**
     * If this player is looping all the songs. This will make the player add newly played tracks back to the queue.
     */
    val loopedAll get() = _loopedAll

    init {
        player.on<TrackExceptionEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error { """msg=Track error" error="${exception.message}"""" }
            updateBoundQueue()
        }

        player.on<TrackStuckEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error { """msg="Track stuck" track="$track" duration="${threshold.inWholeMilliseconds}ms""""" }
        }

        player.on<TrackStartEvent>(CoroutineScope(Dispatchers.IO)) {
            updateBoundQueue()
            updateLastPlayMillis(track.length.inWholeMilliseconds)
        }

        player.on<TrackEndEvent>(CoroutineScope(Dispatchers.IO)) {
            if (looped) {
                queue.offerFirst(track)
            }
            if (loopedAll) {
                queue.offer(track)
            }

            when (reason) {
                TrackEndEvent.EndReason.LOAD_FAILED -> {
                    log.debug { """msg="Track load failed" track="${track.title}"""" }
                    val retried = retryTrack(track)
                    if (retried.not()) {
                        playFromQueue()
                    }
                }
                else -> {
                    log.debug { """msg="Track end" track="${track.title}"""" }
                    playFromQueue()
                }
            }

            updateBoundQueue()
        }
    }

    suspend fun volumeTo(value: Int) {
        if (effects.volume != value) {
            effects.applyVolume(value)
            updateBoundQueue()
        }
    }

    suspend fun toggleLoop() {
        _looped = !_looped
        updateBoundQueue()
    }

    suspend fun toggleLoopAll() {
        _loopedAll = !_loopedAll
        updateBoundQueue()
    }

    /**
     * Adds an audio track request to the queue.
     * @return whether we started playing
     */
    suspend fun add(vararg requests: Track, update: Boolean = false): Boolean {
        for (request in requests) {
            queue.offer(request)
        }
        if (update) {
            updateBoundQueue()
        }
        return play()
    }

    /**
     * Adds an audio track request to the top of the queue.
     */
    suspend fun addFirst(vararg requests: Track, update: Boolean = false): Boolean {
        for (request in requests) {
            queue.offerFirst(request)
        }
        if (update) {
            updateBoundQueue()
        }
        return play()
    }

    /**
     * Plays a song from the queue. Not to be confused with [resume] and [pause].
     * @return whether we started playing
     */
    private suspend fun play(): Boolean {
        if (playing.not()) {
            val track = queue.poll() ?: return false
            updateLastPlayMillis(track.length.inWholeMilliseconds)
            player.playTrack(track)
            return true
        }
        return false
    }

    /**
     * Shuffles the playing track queue.
     */
    suspend fun shuffle() {
        val shuffledQueue = queue.shuffled()

        queue.clear()
        queue.addAll(shuffledQueue)
        updateBoundQueue()
    }

    /**
     * Skips the current song.
     * @return the skipped song, or null if nothing to skip
     */
    suspend fun skip(): Track? {
        return playingTrack?.apply {
            player.stopTrack()
        }
    }

    private val updateBoundQueue = debounce(250) {
        if (boundPaginator == null) return@debounce

        val embedBuilders = buildQueueMessage()
        val mutablePages = (boundPaginator?.pages as MutablePages)

        mutablePages.clear()

        embedBuilders.forEach { embedBuilder ->
            mutablePages.addPage(Page { embedBuilder() })
        }

        boundPaginator?.send()
        log.info { "Queue updated" }
    }

    suspend fun bind(textChannel: MessageChannelBehavior): Snowflake? {
        boundPaginator = messageQueuePaginator(this, textChannel) {
            keepEmbed = false
            val embedBuilders = buildQueueMessage()
            for (embedBuilder in embedBuilders) {
                page { embedBuilder() }
            }
        }
        boundPaginator?.send()
        return boundPaginator?.message?.id
    }

    suspend fun unbind() {
        boundPaginator?.destroy()
        boundPaginator = null
    }

    fun assureConnection() {
        updateLastPlayMillis(0)
    }

    private fun updateLastPlayMillis(trackDuration: Long) {
        val disconnectAllowance = Duration.ofSeconds(DISCONNECT_DURATION).toMillis()
        val newDuration = System.currentTimeMillis() + trackDuration + disconnectAllowance
        lastPlayMillis.lazySet(newDuration)
    }

    /**
     * Removes a certain range in the queue.
     */
    suspend fun remove(start: Int, end: Int): List<Track> {
        if (queue.isEmpty()) return emptyList()

        val startCoerced = start.coerceIn(0, queue.size)
        val endCoerced = end.coerceIn(0, queue.size)
        val removed = queue.toList().subList(startCoerced, endCoerced)

        queue.removeAll(removed.toSet())

        updateBoundQueue()

        return removed
    }

    /**
     * Pause the currently playing track.
     */
    suspend fun pause() {
        player.pause()
        updateBoundQueue()
    }

    /**
     * Resume the currently playing track.
     */
    suspend fun resume() {
        player.unPause()
        updateBoundQueue()
    }

    /**
     * Stop the currently playing track.
     */
    suspend fun stop() {
        player.stopTrack()
    }

    /**
     * Clears the queue.
     */
    suspend fun clear() {
        queue.clear()
        updateBoundQueue()
    }

    private suspend fun playFromQueue() {
        queue.poll()?.let {
            player.playTrack(it)
        }
    }

    /**
     * Builds the queue message.
     */
    fun buildQueueMessage(): List<EmbedBuilder.() -> Unit> {
        val messages = mutableListOf<EmbedBuilder.() -> Unit>()
        val tracksChunked = tracks.chunked(ITEMS_PER_CHUNK)
        var trackNo = 1

        if (tracks.isEmpty()) {
            messages.add(paginatedEmbed(emptyQueueMessage))
        }

        tracksChunked.forEachIndexed { _, requests ->
            val description = buildString {
                requests.forEach { request ->
                    val trackTitle = request.title
                        .abbreviate(EmbedBuilder.Limits.title)
                        .escapedBrackets
                    val trackUri = request.uri
                    val trackDuration = request.length.humanReadableTime
                    val metadata = request.meta

                    if (trackUri?.isUrl == true) {
                        append("`$trackNo.` [$trackTitle]($trackUri) `$trackDuration` <@!${metadata.userId}>\n")
                    } else {
                        val fileExt = trackUri?.split(".")?.last()?.uppercase()
                        append("`$trackNo.` $trackTitle [Local/$fileExt] `$trackDuration` <@!${metadata.userId}>\n")
                    }
                    trackNo++
                }
            }

            messages.add(paginatedEmbed { description })
        }

        return messages.toList()
    }

    private fun paginatedEmbed(embedDescription: () -> String): EmbedBuilder.() -> Unit {
        val nowPlaying = {
            val playingTrackTitle = player.playingTrack?.title?.abbreviate(EmbedBuilder.Limits.title)
            val playingTrackUri = player.playingTrack?.uri
            val playingTrackDuration = player.playingTrack?.length?.humanReadableTime

            if (playingTrackTitle != null) {
                if (playingTrackUri?.isUrl == true) {
                    "[$playingTrackTitle]($playingTrackUri) `$playingTrackDuration`"
                } else {
                    val fileExt = playingTrackUri?.split(".")?.last()?.uppercase()
                    "$playingTrackTitle [Local/$fileExt] `$playingTrackDuration`"
                }
            } else "-"
        }

        return {
            title = "Vivy's Song List"
            color = Color(0x00FFFF)
            description = embedDescription()
            field {
                name = "Now Playing"
                value = nowPlaying()
                inline = false
            }
            field {
                name = "Songs"
                value = tracks.size.toString()
                inline = true
            }
            field {
                name = "Duration"
                value = totalDuration.humanReadableTime
                inline = true
            }
            field {
                name = "Remaining"
                value = remainingDuration.humanReadableTime
                inline = true
            }
            field {
                name = "Looped (Single)"
                value = looped.toYesNo()
                inline = true
            }
            field {
                name = "Looped (All)"
                value = loopedAll.toYesNo()
                inline = true
            }
            field {
                name = "Volume"
                value = "${effects.volume}%"
                inline = true
            }
        }
    }

    private suspend fun retryTrack(track: Track): Boolean {
        val attempts = trackAttempts.getOrPut(track.identifier) { 0 }

        if (attempts < 3) {
            player.playTrack(track)
            trackAttempts[track.identifier] = attempts + 1
            return true
        } else {
            trackAttempts.remove(track.identifier)
            return false
        }
    }
}