package bogus.extension.music.player

import bogus.extension.music.Lava
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Music player using lava link.
 */
class LinkMusicPlayer(guildId: Snowflake) : MusicPlayer(guildId) {
    val link = Lava.linkFor(guildId)
    private val player = link.player.apply {
        on<TrackExceptionEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error(
                throwable = exception,
                msg = "Track error",
                context = mapOf(
                    "errorMessage" to exception.message
                )
            )

            updateBoundQueue()
        }

        on<TrackStuckEvent>(CoroutineScope(Dispatchers.IO)) {
            log.error(
                msg = "Track stuck",
                context = mapOf(
                    "track" to track,
                    "duration" to "${threshold.inWholeMilliseconds}ms"
                )
            )
        }

        on<TrackStartEvent>(CoroutineScope(Dispatchers.IO)) {
            updateBoundQueue()
            if (track.isSeekable) {
                updateLastPlayMillis(track.length.inWholeMilliseconds)
            }
        }

        on<TrackEndEvent>(CoroutineScope(Dispatchers.IO)) {
            if (looped) {
                queue.offerFirst(track.asMusicTrack())
            }
            if (loopedAll) {
                queue.offer(track.asMusicTrack())
            }

            when (reason) {
                TrackEndEvent.EndReason.LOAD_FAILED -> {

                    log.debug(
                        msg = "Track load failed",
                        context = mapOf(
                            "track" to track.title
                        )
                    )

                    val retried = retryTrack(track.asMusicTrack())
                    if (retried.not()) {
                        playFromQueue()
                    }
                }
                else -> {
                    log.debug(
                        msg = "Track end",
                        context = mapOf(
                            "track" to track.title
                        )
                    )
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

    override suspend fun stop() {
        queue.clear()
        player.stopTrack()
        updateBoundQueue()
    }

    override fun findPlayingTrack(): MusicTrack? {
        return player.playingTrack?.asMusicTrack()
    }

    override suspend fun playTrack(track: MusicTrack) {
        player.playTrack(track.track as Track)
    }

    override suspend fun stopTrack() {
        player.stopTrack()
    }
}