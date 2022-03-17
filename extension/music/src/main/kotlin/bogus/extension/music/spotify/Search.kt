package bogus.extension.music.spotify

import bogus.util.urlEncode

data class Search(
    override val uri: String,
    val query: String
) : SpotifyURI(uri) {
    val type = "search"

    override fun toURI(): String {
        return "spotify:$type:${query.urlEncode()}"
    }

    override fun toURL(): String {
        return "/$type/${query.urlEncode()}"
    }
}