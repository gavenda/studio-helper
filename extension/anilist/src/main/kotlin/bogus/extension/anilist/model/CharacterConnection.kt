package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterConnection(
    val nodes: List<bogus.extension.anilist.model.Character?>? = null,
    val edges: List<bogus.extension.anilist.model.CharacterEdge?>? = null
)
