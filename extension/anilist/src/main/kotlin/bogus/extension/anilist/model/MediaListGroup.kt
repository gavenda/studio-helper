package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * List group of anime or manga entries
 */
@Serializable
data class MediaListGroup(
    /**
     * Media list entries
     */
    val entries: List<bogus.extension.anilist.model.MediaList?>?
)
