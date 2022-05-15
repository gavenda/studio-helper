package bogus.extension.music.player

interface TrackLoader {
    suspend fun loadItem(identifier: String): TrackLoadResponse
}

enum class TrackLoadType {
    /**
     * Returned when a single track is loaded.
     */
    TRACK_LOADED,

    /**
     * Returned when a playlist is loaded.
     */
    PLAYLIST_LOADED,

    /**
     * Returned when a search result is made (i.e ytsearch: some song).
     */
    SEARCH_RESULT,

    /**
     * Returned if no matches/sources could be found for a given identifier.
     */
    NO_MATCHES,

    /**
     * Returned if it failed to load something for some reason.
     */
    LOAD_FAILED
}