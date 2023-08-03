package bogus.extension.music.player

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
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
        title = info.title,
        author = info.author,
        length = info.length.milliseconds,
        identifier = info.identifier,
        streamable = info.isStream,
        seekable = info.isSeekable,
        uri = info.uri ?: "",
        position = info.position.milliseconds,
        source = info.sourceName,
        artworkUri = info.artworkUrl ?: ""
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
        artworkUri = info.artworkUrl ?: ""
    )
}