package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The format the media was released in
 */
@Serializable
enum class MediaFormat(val displayName: String) {
    MANGA("Manga"),
    MOVIE("Movie"),
    MUSIC("Music"),
    NOVEL("Novel"),
    ONA("ONA"),
    ONE_SHOT("Oneshot"),
    OVA("OVA"),
    SPECIAL("Special"),
    TV("TV"),
    TV_SHORT("TV Short"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
