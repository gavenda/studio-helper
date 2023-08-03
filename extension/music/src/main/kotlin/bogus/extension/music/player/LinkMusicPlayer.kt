package bogus.extension.music.player

import bogus.extension.music.Lava
import bogus.extension.music.Metric
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Music player using lava link.
 */
class LinkMusicPlayer(guildId: Snowflake) : MusicPlayer(guildId) {
    val link = Lava.linkFor(guildId)
    private val player = link.player.apply {
        on<TrackExceptionEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error { "Track error" }
            updateBoundQueue()
        }

        on<TrackStuckEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error { "Track stuck" }
        }

        on<TrackStartEvent>(CoroutineScope(Dispatchers.IO)) {
            updateBoundQueue()
            if (track.info.isSeekable) {
                updateLastPlayMillis(track.info.length)
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
                Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.LOAD_FAILED -> {

                    log.debug { "Track load failed" }

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
                    log.debug { "Track end" }
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
    }
}