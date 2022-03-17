package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class UserStatusStatistic(
    val count: Int = 0,
    val status: bogus.extension.anilist.model.MediaListStatus = bogus.extension.anilist.model.MediaListStatus.UNKNOWN
)
