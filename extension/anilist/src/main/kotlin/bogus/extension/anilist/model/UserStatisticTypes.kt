package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatisticTypes(
    val anime: bogus.extension.anilist.model.UserStatistics,
    val manga: bogus.extension.anilist.model.UserStatistics
)
