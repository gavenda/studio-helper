package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusStatistic(
    val count: Int = 0,
    val status: MediaListStatus = MediaListStatus.UNKNOWN
)
