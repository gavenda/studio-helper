package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * A user's general options
 */
@Serializable
data class UserOptions(
    /**
     * Profile highlight color (blue, purple, pink, orange, red, green, gray)
     */
    val profileColor: String = ""
)
