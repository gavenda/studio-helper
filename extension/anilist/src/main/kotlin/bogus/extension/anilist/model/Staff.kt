package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Voice actors or production staff
 */
@Serializable
data class Staff(
    /**
     * The id of the staff member
     */
    val id: Long = 0,
    /**
     * The names of the staff member
     */
    val name: bogus.extension.anilist.model.StaffName? = null,
    /**
     * The primary language of the staff member. Current values: Japanese, English, Korean, Italian,
     * Spanish, Portuguese, French, German, Hebrew, Hungarian, Chinese, Arabic, Filipino, Catalan
     */
    val languageV2: String = "",
    /**
     * The staff images
     */
    val image: bogus.extension.anilist.model.StaffImage? = null,
    /**
     * A general description of the staff member
     */
    val description: String = "",
    /**
     * The url for the staff page on the AniList website
     */
    val siteUrl: String = "",
    /**
     * Media where the staff member has a production role
     */
    val staffMedia: bogus.extension.anilist.model.MediaConnection? = null,
    /**
     * Characters voiced by the actor
     */
    val characters: bogus.extension.anilist.model.CharacterConnection? = null,
    /**
     * The amount of user's who have favourited the staff member
     */
    val favourites: Int = 0
)
