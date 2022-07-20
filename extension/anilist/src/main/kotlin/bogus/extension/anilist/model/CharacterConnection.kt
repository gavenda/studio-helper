package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterConnection(
    val nodes: List<Character?>? = null,
    val edges: List<CharacterEdge?>? = null
)
