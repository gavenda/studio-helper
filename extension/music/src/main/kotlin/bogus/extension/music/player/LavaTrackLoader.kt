package bogus.extension.music.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.coroutines.suspendCoroutine

class LavaTrackLoader(private val playerManager: AudioPlayerManager) : TrackLoader {
    override suspend fun loadItem(identifier: String): TrackLoadResponse {
        val trackLoadResponse = TrackLoadResponse(
            loadType = TrackLoadType.LOAD_FAILED,
            track = MusicTrack.EMPTY,
            tracks = listOf(),
            playlistInfo = TrackPlaylistInfo(""),
            error = null
        )

        return suspendCoroutine { continuation ->
            playerManager.loadItem(identifier, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    continuation.resumeWith(
                        Result.success(
                            trackLoadResponse.copy(
                                loadType = TrackLoadType.TRACK_LOADED,
                                track = track.asMusicTrack()
                            )
                        )
                    )
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if (playlist.isSearchResult) {
                        continuation.resumeWith(
                            Result.success(
                                trackLoadResponse.copy(
                                    loadType = TrackLoadType.SEARCH_RESULT,
                                    tracks = playlist.tracks.map { it.asMusicTrack() },
                                    playlistInfo = TrackPlaylistInfo(playlist.name)
                                )
                            )
                        )
                    } else {
                        continuation.resumeWith(
                            Result.success(
                                trackLoadResponse.copy(
                                    loadType = TrackLoadType.PLAYLIST_LOADED,
                                    tracks = playlist.tracks.map { it.asMusicTrack() },
                                    playlistInfo = TrackPlaylistInfo(playlist.name)
                                )
                            )
                        )
                    }
                }

                override fun noMatches() {
                    continuation.resumeWith(
                        Result.success(
                            trackLoadResponse.copy(
                                loadType = TrackLoadType.NO_MATCHES
                            )
                        )
                    )
                }

                override fun loadFailed(exception: FriendlyException) {
                    continuation.resumeWith(
                        Result.success(
                            trackLoadResponse.copy(
                                loadType = TrackLoadType.LOAD_FAILED,
                                error = exception.message
                            )
                        )
                    )
                }
            })
        }

    }
}