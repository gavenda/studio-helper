package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaCoverImage(
    /**
     * The cover image url of the media at its largest size. If this size isn't available, large will
     * be provided instead.
     */
    val extraLarge: String?
)
