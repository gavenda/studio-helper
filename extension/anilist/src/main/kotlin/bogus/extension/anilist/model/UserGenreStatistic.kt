package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserGenreStatistic(
    val count: Int = 0,
    val genre: String = "",
    val meanScore: Float = 0f,
    val minutesWatched: Int = 0,
    val chaptersRead: Int = 0
)
