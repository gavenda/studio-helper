package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserTagStatistic(
    val count: Int = 0,
    val tag: Tag = Tag(0, ""),
    val meanScore: Float = 0f,
    val minutesWatched: Int = 0,
    val chaptersRead: Int = 0
)