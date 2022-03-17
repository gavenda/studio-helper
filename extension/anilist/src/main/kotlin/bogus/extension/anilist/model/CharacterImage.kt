package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterImage(
    /**
     * The character's image of media at its largest size
     */
    val large: String?
)
