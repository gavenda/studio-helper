package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class StaffImage(
    /**
     * The person's image of media at its largest size
     */
    val large: String?
)
