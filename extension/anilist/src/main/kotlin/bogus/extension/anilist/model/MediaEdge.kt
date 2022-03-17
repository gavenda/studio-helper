package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Media connection edge
 */
@Serializable
data class MediaEdge(
    /**
     * The characters role in the media
     */
    val characterRole: bogus.extension.anilist.model.CharacterRole? = null,
    /**
     * Used for grouping roles where multiple dubs exist for the same language.
     * Either dubbing company name or language variant.
     */
    val staffRole: String = ""
)
