package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * List of anime or manga
 */
@Serializable
data class MediaList(
    /**
     * The id of the media
     */
    val mediaId: Long = 0,
    /**
     * The score of the entry
     */
    val score: Float = 0f,
    val status: bogus.extension.anilist.model.MediaListStatus = bogus.extension.anilist.model.MediaListStatus.UNKNOWN,
    val progress: Int = 0,
    val media: bogus.extension.anilist.model.Media? = null,
    val user: bogus.extension.anilist.model.User? = null
)
