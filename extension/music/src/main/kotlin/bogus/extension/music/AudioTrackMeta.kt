package bogus.extension.music

import dev.kord.common.entity.Snowflake

/**
 * Audio track metadata.
 */
data class AudioTrackMeta(
    /**
     * User mention of the requesting user.
     */
    val mention: String = "",
    /**
     * Snowflake of the user who requested the track.
     */
    val userId: Snowflake
)
