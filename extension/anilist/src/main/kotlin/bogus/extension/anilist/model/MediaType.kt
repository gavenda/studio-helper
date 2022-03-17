package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Media type enum, anime or manga.
 */
@Serializable
enum class MediaType(val displayName: String) {
    ANIME("Anime"),
    MANGA("Manga"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
