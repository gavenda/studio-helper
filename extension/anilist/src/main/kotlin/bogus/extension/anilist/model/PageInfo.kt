package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    /**
     * The total number of items
     */
    val total: Int = 0,
    /**
     * The current page
     */
    val currentPage: Int = 0,
    /**
     * The last page
     */
    val lastPage: Int = 0,
    /**
     * If there is another page
     */
    val hasNextPage: Boolean = false,
    /**
     * The count on a page
     */
    val perPage: Int = 0
)
