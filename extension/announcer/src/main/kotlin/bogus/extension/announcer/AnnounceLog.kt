package bogus.extension.announcer

import java.time.Instant

data class AnnounceLog(
    val mention: String,
    val announced: String,
    val timestamp: Instant
)