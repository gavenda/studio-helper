package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Page of data
 */
@Serializable
data class Page(
    /**
     * The pagination information
     */
    val pageInfo: bogus.extension.anilist.model.PageInfo,
    val characters: List<bogus.extension.anilist.model.Character> = listOf(),
    val media: List<bogus.extension.anilist.model.Media> = listOf(),
    val staff: List<bogus.extension.anilist.model.Staff> = listOf(),
    val studios: List<bogus.extension.anilist.model.Studio> = listOf(),
    val users: List<bogus.extension.anilist.model.User> = listOf(),
    val mediaList: List<bogus.extension.anilist.model.MediaList> = listOf()
)
