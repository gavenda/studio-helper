package bogus.extension.announcer

import kotlinx.serialization.Serializable

@Serializable
data class AudioKronMap(
    val kron: String,
    val filePath: String
)
