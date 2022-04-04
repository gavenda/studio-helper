package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class AiringSchedule(
    val id: Long,
    val mediaId: Long,
    val episode: Int,
    val media: Media
)