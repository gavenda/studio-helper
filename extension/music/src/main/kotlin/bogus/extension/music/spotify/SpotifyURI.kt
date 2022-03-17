package bogus.extension.music.spotify

abstract class SpotifyURI(open val uri: String) {
    abstract fun toURL(): String
    abstract fun toURI(): String

    fun toEmbedURL(): String {
        return "https://embed.spotify.com/?uri=${this.toURI()}"
    }

    fun toOpenURL(): String {
        return "http://open.spotify.com${this.toURL()}"
    }

    fun toPlayURL(): String {
        return "https://play.spotify.com${this.toURL()}"
    }
}