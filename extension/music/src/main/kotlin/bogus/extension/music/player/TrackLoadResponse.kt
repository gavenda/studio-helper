package bogus.extension.music.player

data class TrackLoadResponse(
    val loadType: TrackLoadType,
    val track: MusicTrack,
    val tracks: List<MusicTrack> = listOf(),
    val playlistInfo: TrackPlaylistInfo,
    val error: String?
)

data class TrackPlaylistInfo(
    val name: String
)