package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Int = 0,
    val name: String = ""
)