package bogus.extension.music.spotify

import bogus.util.urlEncode

data class User(
    override val uri: String,
    val user: String
) : SpotifyURI(uri) {
    val type = "user"

    override fun toURI(): String {
        return "spotify:$type:${user.urlEncode()}"
    }

    override fun toURL(): String {
        return "/$type/${user.urlEncode()}"
    }
}