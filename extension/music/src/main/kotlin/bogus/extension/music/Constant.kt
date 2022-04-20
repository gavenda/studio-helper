package bogus.extension.music

import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull

const val EXTENSION_NAME = "music"
const val TRANSLATION_BUNDLE = "music"
const val MUSIC_DIRECTORY = "music"
const val DISCONNECT_DURATION = 180L
const val VOLUME_MAX = 100
const val VOLUME_MIN = 0
const val NIGHTCORE_MAX = 300
const val NIGHTCORE_MIN = 10
const val PLAY_NEXT_LIMIT = "play:next"
const val PLAY_NOW_LIMIT = "play:now"

val SPOTIFY_ENABLED: Boolean get() {
    return envOrNull("SPOTIFY_CLIENT_ID") == null
            || envOrNull("SPOTIFY_CLIENT_SECRET") == null
            || env("SPOTIFY_CLIENT_ID").isBlank()
            || env("SPOTIFY_CLIENT_SECRET").isBlank()
}