package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * A user's avatars
 */
@Serializable
data class UserAvatar(
    /**
     * The avatar of user at its largest size
     */
    val large: String = ""
)
