package bogus.extension.music.spotify

import bogus.util.urlEncode

data class Local(
    override val uri: String,
    val artist: String,
    val album: String,
    val track: String,
    val seconds: Int
) : SpotifyURI(uri) {

    override fun toURL(): String {
        return "/local/${artist.urlEncode()}/${album.urlEncode()}/${track.urlEncode()}/$seconds"
    }

    override fun toURI(): String {
        return "spotify:local:${artist.urlEncode()}:${album.urlEncode()}:${track.urlEncode()}:$seconds"
    }
}