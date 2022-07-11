package bogus.extension.announcer

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncerConfig(
    val delay: Long,
    val fileMapping: List<AudioFileMap>,
    val userMapping: Map<Long, List<String>>
)