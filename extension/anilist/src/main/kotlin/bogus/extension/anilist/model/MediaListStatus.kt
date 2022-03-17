package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Media list watching/reading status enum.
 */
@Serializable
enum class MediaListStatus(val displayName: String) {
    COMPLETED("Completed"),
    CURRENT("Current"),
    DROPPED("Dropped"),
    PAUSED("Paused"),
    PLANNING("Planning"),
    REPEATING("Repeating"),
    NOT_ON_LIST("Not On List"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
