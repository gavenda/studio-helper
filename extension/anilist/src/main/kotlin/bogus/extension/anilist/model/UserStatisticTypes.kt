package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatisticTypes(
    val anime: UserStatistics,
    val manga: UserStatistics
)
