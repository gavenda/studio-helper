package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatistics(
    val count: Int = 0,
    val meanScore: Float = 0f,
    val standardDeviation: Float = 0f,
    val minutesWatched: Int = 0,
    val episodesWatched: Int = 0,
    val chaptersRead: Int = 0,
    val volumesRead: Int = 0,
    val formats: List<UserFormatStatistic> = listOf(),
    val statuses: List<UserStatusStatistic> = listOf(),
    val releaseYears: List<UserReleaseYearStatistic> = listOf(),
    val startYears: List<UserStartYearStatistic> = listOf(),
    val genres: List<UserGenreStatistic> = listOf(),
    val tags: List<UserTagStatistic> = listOf()
)
