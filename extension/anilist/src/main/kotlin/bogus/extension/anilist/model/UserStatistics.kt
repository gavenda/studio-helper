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
    val formats: List<bogus.extension.anilist.model.UserFormatStatistic> = listOf(),
    val statuses: List<bogus.extension.anilist.model.UserStatusStatistic> = listOf(),
    val releaseYears: List<bogus.extension.anilist.model.UserReleaseYearStatistic> = listOf(),
    val startYears: List<bogus.extension.anilist.model.UserStartYearStatistic> = listOf(),
    val genres: List<bogus.extension.anilist.model.UserGenreStatistic> = listOf()
)
