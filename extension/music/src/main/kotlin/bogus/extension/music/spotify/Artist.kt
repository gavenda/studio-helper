package bogus.extension.music.spotify

import bogus.util.urlEncode

data class Artist(
    override val uri: String,
    val id: String
) : SpotifyURI(uri) {
    val type = "artist"

    override fun toURI(): String {
        return "spotify:$type:${id.urlEncode()}"
    }

    override fun toURL(): String {
        return "/$type/${id.urlEncode()}"
    }
}