package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Animation or production company
 */
@Serializable
data class Studio(
    /**
     * The id of the studio
     */
    val id: Long,
    /**
     * The name of the studio
     */
    val name: String,
    /**
     * If the studio is an animation studio or a different kind of company
     */
    val isAnimationStudio: Boolean,
    /**
     * The media the studio has worked on
     */
    val media: MediaConnection?,
    /**
     * The url for the studio page on the AniList website
     */
    val siteUrl: String?,
    /**
     * The amount of user's who have favourited the studio
     */
    val favourites: Int?
)
