package bogus.extension.music.spotify

import bogus.util.urlEncode

data class Playlist(
    override val uri: String,
    val id: String,
    val user: String? = null
) : SpotifyURI(uri) {
    val type = "list"

    override fun toURL(): String {
        if (user != null) {
            if (id === "starred") {
                return "/user/${user.urlEncode()}/${id.urlEncode()}}"
            }
            return "/user/${user.urlEncode()}/$type/${id.urlEncode()}"
        }
        return "/$type/${id.urlEncode()}"
    }

    override fun toURI(): String {
        if (user != null) {
            if (id === "starred") {
                return "spotify:user:${user.urlEncode()}:${id.urlEncode()}"
            }
            return "spotify:user:${user.urlEncode()}:$type:${id.urlEncode()}"
        }
        return "spotify:$type:${id.urlEncode()}"
    }
}