package bogus.extension.announcer

import kotlinx.serialization.Serializable

@Serializable
data class AudioFileMap(
    val name: String,
    val filePath: String
)
