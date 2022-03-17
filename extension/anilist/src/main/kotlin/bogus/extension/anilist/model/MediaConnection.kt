package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaConnection(
    val nodes: List<bogus.extension.anilist.model.Media?>? = null,
    val edges: List<bogus.extension.anilist.model.MediaEdge?>? = null,
    val pageInfo: bogus.extension.anilist.model.PageInfo? = null
)
