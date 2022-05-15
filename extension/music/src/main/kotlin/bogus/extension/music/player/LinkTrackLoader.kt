package bogus.extension.music.player

import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.lavakord.rest.mapToTrack

class LinkTrackLoader(private val link: Link) : TrackLoader {

    override suspend fun loadItem(identifier: String): TrackLoadResponse {
        val item = link.loadItem(identifier)
        return TrackLoadResponse(
            loadType = toLoadType(item.loadType),
            track = item.tracks.first().toTrack().asMusicTrack(),
            tracks = item.tracks.mapToTrack().map { it.asMusicTrack() },
            error = item.exception?.message,
            playlistInfo = TrackPlaylistInfo(item.playlistInfo.name ?: "")
        )
    }

    private fun toLoadType(loadType: TrackResponse.LoadType): TrackLoadType {
        return when (loadType) {
            TrackResponse.LoadType.LOAD_FAILED -> TrackLoadType.LOAD_FAILED
            TrackResponse.LoadType.TRACK_LOADED -> TrackLoadType.TRACK_LOADED
            TrackResponse.LoadType.PLAYLIST_LOADED -> TrackLoadType.PLAYLIST_LOADED
            TrackResponse.LoadType.SEARCH_RESULT -> TrackLoadType.SEARCH_RESULT
            TrackResponse.LoadType.NO_MATCHES -> TrackLoadType.NO_MATCHES
        }
    }

}