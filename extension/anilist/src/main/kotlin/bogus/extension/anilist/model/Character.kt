package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * A character that features in an anime or manga
 */
@Serializable
data class Character(
    /**
     * The id of the character
     */
    val id: Long = 0,
    /**
     * The names of the character
     */
    val name: CharacterName? = null,
    /**
     * Character images
     */
    val image: CharacterImage? = null,
    /**
     * A general description of the character
     */
    val description: String = "",
    /**
     * The url for the character page on the AniList website
     */
    val siteUrl: String = "",
    /**
     * Media that includes the character
     */
    val media: bogus.extension.anilist.model.MediaConnection? = null,
    /**
     * The amount of user's who have favourited the character
     */
    val favourites: Int = 0
)
