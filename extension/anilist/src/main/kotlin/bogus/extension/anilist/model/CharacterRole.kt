package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The role the character plays in the media
 */
@Serializable
enum class CharacterRole(val displayName: String) {
    BACKGROUND("Background"),
    MAIN("Main"),
    SUPPORTING("Supporting"),

    /**
     * This is a default enum value that will be used when attempting to deserialize unknown value.
     */
    UNKNOWN("Unknown"),
}
