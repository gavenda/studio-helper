package bogus.extension.music.player

import bogus.extension.music.FRAME_BUFFER_SIZE
import bogus.extension.music.Metric
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

    override fun findPlayingTrack(): MusicTrack? {
        return player.playingTrack?.asMusicTrack()
    }

    override suspend fun playTrack(track: MusicTrack) {
        player.playTrack((track.track as AudioTrack).makeClone())
        playingTrackTo(track)
        registry.counter(Metric.SONGS_PLAYED).increment()
    }

    override suspend fun stopTrack() {
        player.stopTrack()
        clearPlayingTrack()
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
        if (musicTrack.seekable) {
            updateLastPlayMillis(musicTrack.length.inWholeMilliseconds)
        }
    }

    private suspend fun onTrackEnd(track: AudioTrack, endReason: AudioTrackEndReason) {
        clearPlayingTrack()

        val musicTrack = track.asMusicTrack()
        val currentTrack = playingTrack

        if (looped) {
            queue.offerFirst(track.asMusicTrack(true))
        }
        if (loopedAll) {
            queue.offer(track.asMusicTrack(true))
        }

        when (endReason) {
            AudioTrackEndReason.LOAD_FAILED -> {
                log.debug { "Track load failed" }

                val trackToRetry = if (currentTrack != null && musicTrack.uri == currentTrack.uri) {
                    currentTrack
                } else musicTrack

                val retried = retryTrack(trackToRetry)
                if (retried.not()) {
                    playFromQueue()
                }
            }
            else -> {
                log.debug { "Track end" }
                playFromQueue()
            }
        }

        updateBoundQueue()
    }

    private fun onTrackException(exception: FriendlyException) {
        log.error(exception) { "Track error" }

        updateBoundQueue()
    }

    private fun onTrackStuck(track: AudioTrack, thresholdMs: Long) {
        log.error { "Track stuck" }
    }
}