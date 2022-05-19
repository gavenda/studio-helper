package bogus.extension.music.player

import bogus.extension.music.FRAME_BUFFER_SIZE
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import java.nio.ByteBuffer

/**
 * Music player using lava player locally.
 */
@OptIn(KordVoice::class)
class LavaMusicPlayer(guildId: Snowflake) : MusicPlayer(guildId), AudioEventListener {
    private val playerManager by inject<AudioPlayerManager>()
    private val buffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    private val frame = MutableAudioFrame().apply { setBuffer(buffer) }
    private val player = playerManager.createPlayer()
    private var voiceConnection: VoiceConnection? = null

    init {
        player.addListener(this)
    }

    val audioProvider: () -> AudioFrame? = {
        val canProvide = player.provide(frame)
        if (canProvide) {
            AudioFrame.fromData(buffer.flip().moveToByteArray())
        } else {
            AudioFrame.fromData(null)
        }
    }

    fun useVoiceConnection(voiceConnection: VoiceConnection) {
        this.voiceConnection = voiceConnection
    }

    override val paused: Boolean
        get() = player.isPaused
    override val effects: MusicEffects = LavaMusicEffects(player, playerManager.configuration)
    override val loader: TrackLoader = LavaTrackLoader(playerManager)

    override suspend fun disconnect() {
        voiceConnection?.leave()
    }

    override suspend fun pause() {
        player.isPaused = true
        updateBoundQueue()
    }

    override suspend fun resume() {
        player.isPaused = false
        updateBoundQueue()
    }

    override suspend fun stop() {
        queue.clear()
        player.stopTrack()
    }

    override fun findPlayingTrack(): MusicTrack? {
        return player.playingTrack?.asMusicTrack()
    }

    override suspend fun playTrack(track: MusicTrack) {
        player.playTrack((track.track as AudioTrack).makeClone())
    }

    override suspend fun stopTrack() {
        player.stopTrack()
    }

    override fun onEvent(event: AudioEvent) = runBlocking {
        when (event) {
            is TrackStartEvent -> {
                onTrackStart(event.track)
            }
            is TrackEndEvent -> {
                onTrackEnd(event.track, event.endReason)
            }
            is TrackExceptionEvent -> {
                onTrackException(event.exception)
            }
            is TrackStuckEvent -> {
                onTrackStuck(event.track, event.thresholdMs)
            }
        }
    }

    private fun onTrackStart(track: AudioTrack) {
        updateBoundQueue()
        val musicTrack = track.asMusicTrack()
        if (musicTrack.isSeekable) {
            updateLastPlayMillis(musicTrack.length.inWholeMilliseconds)
        }
    }

    private suspend fun onTrackEnd(track: AudioTrack, endReason: AudioTrackEndReason) {
        val musicTrack = track.asMusicTrack()

        if (looped) {
            queue.offerFirst(track.asMusicTrack(true))
        }
        if (loopedAll) {
            queue.offer(track.asMusicTrack(true))
        }

        when (endReason) {
            AudioTrackEndReason.LOAD_FAILED -> {
                log.debug(
                    msg = "Track load failed",
                    context = mapOf(
                        "track" to musicTrack.title
                    )
                )

                val retried = retryTrack(musicTrack)
                if (retried.not()) {
                    playFromQueue()
                }
            }
            else -> {
                log.debug(
                    msg = "Track end",
                    context = mapOf(
                        "track" to musicTrack.title
                    )
                )
                playFromQueue()
            }
        }

        updateBoundQueue()
    }

    private fun onTrackException(exception: FriendlyException) {
        log.error(
            throwable = exception,
            msg = "Track error",
            context = mapOf(
                "errorMessage" to exception.message
            )
        )

        updateBoundQueue()
    }

    private fun onTrackStuck(track: AudioTrack, thresholdMs: Long) {
        log.error(
            msg = "Track stuck",
            context = mapOf(
                "track" to track,
                "duration" to "${thresholdMs}ms"
            )
        )
    }
}