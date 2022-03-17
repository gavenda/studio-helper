package bogus.extension.music.spotify

import bogus.util.urlEncode

data class Show(
    override val uri: String,
    val id: String
) : SpotifyURI(uri) {
    val type = "show"

    override fun toURI(): String {
        return "spotify:$type:${id.urlEncode()}"
    }

    override fun toURL(): String {
        return "/$type/${id.urlEncode()}"
    }
}