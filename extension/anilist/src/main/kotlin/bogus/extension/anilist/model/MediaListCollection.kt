package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * List of anime or manga
 */
@Serializable
data class MediaListCollection(
    /**
     * The owner of the list
     */
    val user: bogus.extension.anilist.model.User?,
    /**
     * Grouped media list entries
     */
    val lists: List<bogus.extension.anilist.model.MediaListGroup?>?
)
