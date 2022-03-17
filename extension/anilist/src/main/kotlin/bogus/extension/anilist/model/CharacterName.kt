package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The names of the character
 */
@Serializable
data class CharacterName(
    /**
     * The character's first and last name
     */
    val full: String? = null,
    /**
     * The character's full name in their native language
     */
    val native: String? = null,
    /**
     * Other names the character might be referred to as
     */
    val alternative: List<String?>? = null
)
