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
    val pageInfo: PageInfo,
    val characters: List<Character> = listOf(),
    val media: List<Media> = listOf(),
    val staff: List<Staff> = listOf(),
    val studios: List<Studio> = listOf(),
    val users: List<User> = listOf(),
    val mediaList: List<MediaList> = listOf(),
    val airingSchedules: List<AiringSchedule> = listOf()
)
