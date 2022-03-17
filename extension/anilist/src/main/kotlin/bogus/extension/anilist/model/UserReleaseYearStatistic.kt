package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserReleaseYearStatistic(
    val count: Int = 0,
    val releaseYear: Int = 0
)
