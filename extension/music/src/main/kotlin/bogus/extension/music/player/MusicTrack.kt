package bogus.extension.music.player

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.audio.player.Track
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class MusicTrack(
    val track: Any,
    val title: String,
    val author: String,
    val length: Duration,
    val identifier: String,
    val isStream: Boolean,
    val isSeekable: Boolean,
    val uri: String,
    val position: Duration,
    val userId: Snowflake = Snowflake(0),
    val mention: String = ""
) {
    companion object {
        val EMPTY = MusicTrack(
            track = false,
            title = "",
            author = "",
            length = 0.milliseconds,
            identifier = "",
            isStream = false,
            isSeekable = false,
            uri = "",
            position = 0.milliseconds,
        )
    }
}

fun Track.asMusicTrack(): MusicTrack {
    return MusicTrack(
        track = this,
        title = title,
        author = author,
        length = length,
        identifier = identifier,
        isStream = isStream,
        isSeekable = isSeekable,
        uri = uri ?: "",
        position = position
    )
}

fun AudioTrack.asMusicTrack(): MusicTrack {
    return MusicTrack(
        track = this,
        title = info.title,
        author = info.author,
        length = info.length.milliseconds,
        identifier = info.identifier,
        isStream = info.isStream,
        isSeekable = isSeekable,
        uri = info.uri,
        position = position.milliseconds
    )
}