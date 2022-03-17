package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The names of the staff member
 */
@Serializable
data class StaffName(
    /**
     * The person's first and last name
     */
    val full: String?,
    /**
     * The person's full name in their native language
     */
    val native: String?,
    /**
     * Other names the staff member might be referred to as (pen names)
     */
    val alternative: List<String?>?
)
