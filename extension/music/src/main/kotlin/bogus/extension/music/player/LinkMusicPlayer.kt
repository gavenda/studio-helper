package bogus.extension.music.player

import bogus.extension.music.Lava
import bogus.extension.music.Metric
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.Track
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.inject

/**
 * Music player using lava link.
 */
class LinkMusicPlayer(guildId: Snowflake) : MusicPlayer(guildId) {
    val link = Lava.linkFor(guildId)
    private val player = link.player.apply {
        on<TrackExceptionEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error(exception) {
                message = "Track error"
                context = mapOf(
                    "errorMessage" to exception.message
                )
            }

            updateBoundQueue()
        }

        on<TrackStuckEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error {
                message = "Track stuck"
                context = mapOf(
                    "track" to track,
                    "duration" to "${threshold.inWholeMilliseconds}ms"
                )
            }
        }

        on<TrackStartEvent>(CoroutineScope(Dispatchers.IO)) {
            updateBoundQueue()
            if (track.isSeekable) {
                updateLastPlayMillis(track.length.inWholeMilliseconds)
            }
        }

        on<TrackEndEvent>(CoroutineScope(Dispatchers.IO)) {
            clearPlayingTrack()

            if (looped) {
                queue.offerFirst(track.asMusicTrack())
            }
            if (loopedAll) {
                queue.offer(track.asMusicTrack())
            }

            when (reason) {
                TrackEndEvent.EndReason.LOAD_FAILED -> {

                    log.debug {
                        message = "Track load failed"
                        context = mapOf(
                            "track" to track.title
                        )
                    }

                    val musicTrack = track.asMusicTrack()
                    val currentTrack = this@LinkMusicPlayer.playingTrack
                    val trackToRetry = if (currentTrack != null && musicTrack.uri == currentTrack.uri) {
                        currentTrack
                    } else musicTrack

                    val retried = retryTrack(trackToRetry)
                    if (retried.not()) {
                        playFromQueue()
                    }
                }
                else -> {
                    log.debug {
                        message = "Track end"
                        context = mapOf(
                            "track" to track.title
                        )
                    }
                    playFromQueue()
                }
            }

            updateBoundQueue()
        }
    }

    override val paused: Boolean
        get() = player.paused
    override val effects: MusicEffects = LinkMusicEffects(player)
    override val loader: TrackLoader = LinkTrackLoader(link)

    override suspend fun disconnect() {
        link.disconnectAudio()
    }

    override suspend fun pause() {
        player.pause()
        updateBoundQueue()
    }

    override suspend fun resume() {
        player.unPause()
        updateBoundQueue()
    }

    override fun findPlayingTrack(): MusicTrack? {
        return player.playingTrack?.asMusicTrack()
    }

    override suspend fun playTrack(track: MusicTrack) {
        player.playTrack(track.track as Track)
        playingTrackTo(track)
        registry.counter(Metric.SONGS_PLAYED).increment()
    }

    override suspend fun stopTrack() {
        player.stopTrack()
        clearPlayingTrack()
        registry.counter(Metric.SONGS_STOPPED).increment()
    }
}