package bogus.extension.music.player

data class TrackLoadResponse(
    val loadType: TrackLoadType,
    val tracks: List<MusicTrack> = listOf(),
    val playlistInfo: TrackPlaylistInfo,
    val error: String?
) {
    val track: MusicTrack get() = tracks.firstOrNull() ?: error("No tracks in response!")
}

data class TrackPlaylistInfo(
    val name: String
)