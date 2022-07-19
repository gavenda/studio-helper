package bogus.extension.music.player

import bogus.constants.ITEMS_PER_CHUNK
import bogus.extension.music.*
import bogus.extension.music.MusicExtension.EMBED_COLOR
import bogus.extension.music.db.guilds
import bogus.extension.music.paginator.MessageQueuePaginator
import bogus.extension.music.paginator.MutablePages
import bogus.extension.music.paginator.messageQueuePaginator
import bogus.util.*
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
abstract class MusicPlayer(val guildId: Snowflake) : KordExKoinComponent {
    private val db by inject<Database>()
    private val tp by inject<TranslationsProvider>()
    protected val queue = LinkedBlockingDeque<MusicTrack>()
    protected val log = KotlinLogging.logger {}.asFMTLogger()
    private val queueUpdatePublisher = MutableSharedFlow<Long>(
        extraBufferCapacity = Channel.UNLIMITED
    )
    protected val queueUpdates = queueUpdatePublisher.asSharedFlow()
    private val trackAttempts = ConcurrentHashMap<String, Int>()
    private val emptyQueueMessage = {
        tp.translate(
            key = "player.queue.message", bundleName = TRANSLATION_BUNDLE
        )
    }

    /**
     * Last played song in milliseconds.
     */
    val lastPlayMillis = atomic(System.currentTimeMillis())

    /**
     * The track request list of this player.
     * @return the track list
     */
    val tracks: List<MusicTrack> get() = queue.toList()

    /**
     * Returns true if the player is currently playing something regardless if it is paused.
     */
    val playing: Boolean
        get() = findPlayingTrack() != null

    /**
     * Returns true if the player is bound to a text channel.
     */
    val bound: Boolean
        get() = boundPaginator != null

    /**
     * The bound text channel id.
     */
    protected var boundPaginator: MessageQueuePaginator? = null

    /**
     * The remaining duration of this player in milliseconds.
     */
    val remainingDuration: Long
        get() {
            var duration = 0L
            val playingTrack = findPlayingTrack()
            if (playingTrack != null) {
                if (playing && playingTrack.seekable) {
                    val delta = playingTrack.length.inWholeMilliseconds - playingTrack.position.inWholeMilliseconds
                    if (delta < 0) {
                        duration = playingTrack.length.inWholeMilliseconds
                    } else {
                        duration = delta
                    }
                }
            }

            return duration + queue.duration
        }

    /**
     * The total duration of this player in milliseconds.
     */
    val totalDuration: Long
        get() {
            var duration = 0L
            val playingTrack = findPlayingTrack()
            if (playingTrack != null) {
                if (playing && playingTrack.seekable) {
                    duration = playingTrack.length.inWholeMilliseconds
                }
            }
            return duration + queue.duration
        }

    private var _looped = false
    private var _loopedAll = false

    /**
     * If this player is looping the current song.
     */
    val looped get() = _looped

    /**
     * If this player is looping all the songs. This will make the player add newly played tracks back to the queue.
     */
    val loopedAll get() = _loopedAll

    init {
        queueUpdates
            .debounce(250)
            .onEach {
                if (boundPaginator == null) return@onEach

                val embedBuilders = buildQueueMessage()
                val mutablePages = (boundPaginator?.pages as MutablePages)

                mutablePages.clear()

                embedBuilders.forEach { embedBuilder ->
                    mutablePages.addPage(Page { embedBuilder() })
                }

                try {
                    boundPaginator?.send()
                    log.debug { message = "Queue updated" }
                } catch (ex: Exception) {
                    log.error(ex) { message = "Unable to send bind message" }
                }
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun toggleLoop() {
        _looped = !_looped
        updateBoundQueue()
    }

    fun toggleLoopAll() {
        _loopedAll = !_loopedAll
        updateBoundQueue()
    }

    /**
     * Adds an audio track request to the queue.
     * @return whether we started playing
     */
    suspend fun add(vararg requests: MusicTrack, update: Boolean = false, play: Boolean = true): Boolean {
        for (request in requests) {
            queue.offer(request)
        }
        if (update) {
            updateBoundQueue()
        }
        if (play) {
            return attemptToPlay()
        }
        return attemptToPlay()
    }

    /**
     * Adds an audio track request to the top of the queue.
     */
    suspend fun addFirst(vararg requests: MusicTrack, update: Boolean = false): Boolean {
        for (request in requests) {
            queue.offerFirst(request)
        }
        if (update) {
            updateBoundQueue()
        }
        return attemptToPlay()
    }

    /**
     * Plays a song from the queue. Not to be confused with [resume] and [pause].
     * @return whether we started playing
     */
    suspend fun attemptToPlay(): Boolean {
        if (playing.not()) {
            val track = queue.poll() ?: return false
            if (track.seekable) {
                updateLastPlayMillis(track.length.inWholeMilliseconds)
            }
            playTrack(track)
            return true
        }
        return false
    }

    /**
     * Shuffles the playing track queue.
     */
    fun shuffle() {
        val shuffledQueue = queue.shuffled()

        queue.clear()
        queue.addAll(shuffledQueue)
        updateBoundQueue()
    }

    /**
     * Skips the current song.
     * @return the skipped song, or null if nothing to skip
     */
    suspend fun skip(): MusicTrack? {
        return findPlayingTrack()?.apply {
            stopTrack()
        }
    }

    fun updateBoundQueue() {
        queueUpdatePublisher.tryEmit(System.currentTimeMillis())
    }

    suspend fun bind(textChannel: MessageChannelBehavior): Snowflake? {
        boundPaginator = messageQueuePaginator(this, textChannel) {
            keepEmbed = false
            val embedBuilders = buildQueueMessage()
            for (embedBuilder in embedBuilders) {
                page { embedBuilder() }
            }
        }
        try {
            boundPaginator?.send()
        } catch (ex: Exception) {
            log.error(ex) { message = "Unable to send bind message" }
        }
        return boundPaginator?.message?.id
    }

    suspend fun unbind() {
        boundPaginator?.destroy()
        boundPaginator = null
    }

    fun assureConnection() {
        updateLastPlayMillis(0)
    }

    /**
     * Update last play millis (for auto disconnect)
     * @param trackDuration the last track duration in milliseconds
     */
    protected fun updateLastPlayMillis(trackDuration: Long) {
        val disconnectAllowance = DISCONNECT_DURATION.seconds.inWholeMilliseconds
        val newDuration = System.currentTimeMillis() + trackDuration + disconnectAllowance
        lastPlayMillis.lazySet(newDuration)
    }

    /**
     * Removes a certain range in the queue.
     */
    fun remove(start: Int, end: Int): List<MusicTrack> {
        if (queue.isEmpty()) return emptyList()

        val startCoerced = start.coerceIn(0, queue.size)
        val endCoerced = end.coerceIn(0, queue.size)
        val removed = queue.toList().subList(startCoerced, endCoerced)

        queue.removeAll(removed.toSet())

        updateBoundQueue()

        return removed
    }

    /**
     * Returns true if the player is paused.
     */
    abstract val paused: Boolean

    /**
     * Tune music effects.
     */
    abstract val effects: MusicEffects

    /**
     * Track loader for this player.
     */
    abstract val loader: TrackLoader

    /**
     * Returns the current volume.
     */
    val volume: Int get() = effects.volume

    /**
     * Pause the currently playing track.
     */
    abstract suspend fun pause()

    /**
     * Resume the currently playing track.
     */
    abstract suspend fun resume()

    /**
     * Stop the currently playing track.
     */
    abstract suspend fun stop()

    /**
     * Returns the currently playing track.
     */
    abstract fun findPlayingTrack(): MusicTrack?

    /**
     * Disconnect from voice channel.
     */
    abstract suspend fun disconnect()

    /**
     * Set volume to specified value.
     * @param value the volume to set
     */
    suspend fun volumeTo(value: Int, update: Boolean = true) {
        effects.applyVolume(value)

        if (update) {
            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guildId.value.toLong() } ?: return

            dbGuild.volume = volume
            dbGuild.flushChanges()

            updateBoundQueue()
        }
    }

    /**
     * Play the given music track.
     */
    abstract suspend fun playTrack(track: MusicTrack)

    /**
     * Stop the currently playing track.
     */
    abstract suspend fun stopTrack()

    /**
     * Clears the queue.
     */
    fun clear() {
        queue.clear()
        updateBoundQueue()
    }

    protected suspend fun playFromQueue() {
        queue.poll()?.let {
            playTrack(it)
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
                    val trackTitle = request.title.abbreviate(EmbedBuilder.Limits.title).escapedBrackets
                    val trackUri = request.uri
                    val trackDuration = request.length.humanReadableTime

                    if (request.source != SOURCE_LOCAL) {
                        append(tp.translate(
                            key = "player.queue.track",
                            bundleName = TRANSLATION_BUNDLE,
                            replacements = arrayOf(trackNo, trackTitle, trackUri, trackDuration, request.userId)
                        ))
                    } else {
                        val fileExt = trackUri.split(".").last().uppercase()
                        append(tp.translate(
                            key = "player.queue.track.local",
                            bundleName = TRANSLATION_BUNDLE,
                            replacements = arrayOf(trackNo, trackTitle, fileExt, trackDuration, request.userId)
                        ))
                    }
                    trackNo++
                }
            }

            messages.add(paginatedEmbed { description })
        }

        return messages.toList()
    }

    private fun paginatedEmbed(embedDescription: () -> String): EmbedBuilder.() -> Unit {
        val playingTrack = findPlayingTrack()
        val nowPlaying = {
            val playingTrackTitle = playingTrack?.title?.abbreviate(EmbedBuilder.Limits.title)
            val playingTrackUri = playingTrack?.uri
            val playingTrackDuration = playingTrack?.length?.humanReadableTime

            if (playingTrackTitle != null) {
                if (playingTrackUri?.isUrl == true) {
                    tp.translate(
                        key = "player.queue.playing-track",
                        bundleName = TRANSLATION_BUNDLE,
                        replacements = arrayOf(playingTrackTitle, playingTrackUri, playingTrackDuration)
                    )
                } else {
                    val fileExt = playingTrackUri?.split(".")?.last()?.uppercase()
                    tp.translate(
                        key = "player.queue.playing-track.local",
                        bundleName = TRANSLATION_BUNDLE,
                        replacements = arrayOf(playingTrackTitle, fileExt, playingTrackDuration)
                    )
                }
            } else "-"
        }

        return {
            title = tp.translate(
                key = "response.jukebox.queue-list",
                bundleName = TRANSLATION_BUNDLE
            )
            color = Color(EMBED_COLOR)
            description = embedDescription()
            image = playingTrack?.artworkUri

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
                value = "${volume}%"
                inline = true
            }
            field {
                name = "Filter(s)"
                value = effects.activeFilters
                inline = true
            }
            field {
                name = "Equalizer"
                value = effects.activeEqualizer
                inline = true
            }
        }
    }

    protected suspend fun retryTrack(track: MusicTrack): Boolean {
        val attempts = trackAttempts.getOrPut(track.identifier) { 0 }

        if (attempts < 3) {
            playTrack(track)
            trackAttempts[track.identifier] = attempts + 1
            return true
        } else {
            trackAttempts.remove(track.identifier)
            return false
        }
    }
}