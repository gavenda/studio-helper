package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The current releasing status of the media
 */
@Serializable
enum class MediaStatus(val displayName: String) {
    CANCELLED("Cancelled"),
    FINISHED("Finished"),
    HIATUS("Hiatus"),
    NOT_YET_RELEASED("Not Yet Released"),
    RELEASING("Releasing"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
