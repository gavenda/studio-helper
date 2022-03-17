package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * A user
 */
@Serializable
data class User(
    /**
     * The id of the user
     */
    val id: Long = 0,
    /**
     * The name of the user
     */
    val name: String = "",
    /**
     * The bio written by user (Markdown)
     */
    val about: String = "",
    /**
     * The user's avatar images
     */
    val avatar: bogus.extension.anilist.model.UserAvatar? = null,
    /**
     * The user's general options
     */
    val options: bogus.extension.anilist.model.UserOptions? = null,
    /**
     * The url for the user page on the AniList website
     */
    val siteUrl: String = "",
    /**
     * The user's banner images
     */
    val bannerImage: String? = null,
    /**
     * The users anime & manga list statistics
     */
    val statistics: bogus.extension.anilist.model.UserStatisticTypes? = null,
    /**
     * When the user's data was last updated
     */
    val updatedAt: Int = 0
)
