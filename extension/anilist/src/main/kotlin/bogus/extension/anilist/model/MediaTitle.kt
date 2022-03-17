package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The official titles of the media in various languages
 */
@Serializable
data class MediaTitle(
    /**
     * The official english title
     */
    val english: String? = null,
    /**
     * The romanization of the native language title
     */
    val romaji: String? = null,
    /**
     * Official title in it's native language
     */
    val native: String? = null
)
