package bogus.extension.music.player

import bogus.extension.music.SOURCE_YOUTUBE
import bogus.extension.music.youtubeThumbnail
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
    val streamable: Boolean,
    val seekable: Boolean,
    val uri: String,
    val position: Duration,
    val userId: Snowflake = Snowflake(0),
    val mention: String = "",
    val source: String = "",
    val artworkUri: String = ""
) {
    companion object {
        val EMPTY = MusicTrack(
            track = false,
            title = "",
            author = "",
            length = 0.milliseconds,
            identifier = "",
            streamable = false,
            seekable = false,
            uri = "",
            position = 0.milliseconds,
        )
    }

    fun makeClone(): MusicTrack {
        if (track is AudioTrack) {
            return copy(track = track.makeClone())
        }
        return copy()
    }
}

fun Track.asMusicTrack(): MusicTrack {
    return MusicTrack(
        track = this,
        title = title,
        author = author,
        length = length,
        identifier = identifier,
        streamable = isStream,
        seekable = isSeekable,
        uri = uri ?: "",
        position = position,
        source = source,
        artworkUri = if (source == SOURCE_YOUTUBE) {
            youtubeThumbnail(identifier)
        } else ""
    )
}

fun AudioTrack.asMusicTrack(clone: Boolean = false): MusicTrack {
    return MusicTrack(
        track = if (clone) makeClone() else this,
        title = info.title,
        author = info.author,
        length = info.length.milliseconds,
        identifier = info.identifier,
        streamable = info.isStream,
        seekable = isSeekable,
        uri = info.uri,
        position = position.milliseconds,
        source = sourceManager.sourceName,
        artworkUri = info.artworkUrl
    )
}