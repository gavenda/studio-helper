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
const val FRAME_BUFFER_SIZE = 1024
const val PLAY_NEXT_LIMIT = "play:next"
const val PLAY_NOW_LIMIT = "play:now"
const val SOURCE_YOUTUBE = "youtube"

val SPOTIFY_ENABLED: Boolean
    get() {
        return envOrNull("SPOTIFY_CLIENT_ID") != null
                && envOrNull("SPOTIFY_CLIENT_SECRET") != null
                && env("SPOTIFY_CLIENT_ID").isNotBlank()
                && env("SPOTIFY_CLIENT_SECRET").isNotBlank()
    }

val LAVAKORD_ENABLED: Boolean
    get() {
        return envOrNull("LINK_NODES") != null
                && envOrNull("LINK_PASSWORDS") != null
    }