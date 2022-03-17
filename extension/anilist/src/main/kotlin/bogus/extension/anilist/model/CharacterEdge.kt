package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Character connection edge
 */
@Serializable
data class CharacterEdge(
    /**
     * The characters role in the media
     */
    val role: bogus.extension.anilist.model.CharacterRole? = null
)
