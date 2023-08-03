package bogus.extension.music.player

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Playlist
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.rest.loadItem

class LinkTrackLoader(private val link: Link) : TrackLoader {

    override suspend fun loadItem(identifier: String): TrackLoadResponse {
        val item = link.loadItem(identifier)
        when (item.loadType) {
            ResultStatus.PLAYLIST -> {
                val playlist = item.data as Playlist
                return TrackLoadResponse(
                    loadType = toLoadType(item.loadType),
                    tracks = playlist.tracks.map { it.asMusicTrack() },
                    error = "",
                    playlistInfo = TrackPlaylistInfo(playlist.info.name)
                )
            }
            ResultStatus.TRACK -> {
                val track = item.data as Track
                return TrackLoadResponse(
                    loadType = toLoadType(item.loadType),
                    tracks = listOf(track.asMusicTrack()),
                    error = null,
                    playlistInfo = TrackPlaylistInfo("")
                )
            }
            ResultStatus.SEARCH -> {
                val searchResult = item.data as LoadResult.SearchResult.Data
                return TrackLoadResponse(
                    loadType = toLoadType(item.loadType),
                    tracks = searchResult.tracks.map { it.asMusicTrack() },
                    error = null,
                    playlistInfo = TrackPlaylistInfo("")
                )
            }
            ResultStatus.NONE -> {
                return TrackLoadResponse(
                    loadType = toLoadType(item.loadType),
                    tracks = listOf(),
                    error = null,
                    playlistInfo = TrackPlaylistInfo("")
                )
            }
            ResultStatus.ERROR -> {
                val error = item.data as Exception
                return TrackLoadResponse(
                    loadType = toLoadType(item.loadType),
                    tracks = listOf(),
                    error = error.message,
                    playlistInfo = TrackPlaylistInfo("")
                )
            }
        }
    }

    private fun toLoadType(loadType: ResultStatus): TrackLoadType {
        return when (loadType) {
            ResultStatus.ERROR -> TrackLoadType.LOAD_FAILED
            ResultStatus.TRACK -> TrackLoadType.TRACK_LOADED
            ResultStatus.PLAYLIST -> TrackLoadType.PLAYLIST_LOADED
            ResultStatus.SEARCH -> TrackLoadType.SEARCH_RESULT
            ResultStatus.NONE -> TrackLoadType.NO_MATCHES
        }
    }

}