package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaSeason(val displayName: String) {
    FALL("Fall"),
    SPRING("Spring"),
    SUMMER("Summer"),
    WINTER("Winter"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
