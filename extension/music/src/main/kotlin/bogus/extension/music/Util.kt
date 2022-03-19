package bogus.extension.music

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import java.util.concurrent.BlockingDeque
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Link instance.
 */
val ApplicationCommandContext.link: Link
    get() {
        return guild?.let { Lava.linkFor(it.id) } ?: error("Missing guild")
    }

/**
 * Player instance.
 */
val ApplicationCommandContext.player: GuildMusicPlayer
    get() {
        return guild?.let { Jukebox.playerFor(it.id) } ?: error("Missing guild")
    }

/**
 * Guild lava link instance.
 */
val GuildBehavior.link: Link
    get() {
        return Lava.linkFor(id)
    }

/**
 * Guild music player.
 */
val GuildBehavior.player: GuildMusicPlayer
    get() {
        return Jukebox.playerFor(id)
    }

/**
 * Audio track metadata.
 */
var Track.meta
    get(): AudioTrackMeta = Lava.metaFor(this) ?: error("Meta not found")
    set(value) {
        Lava.attachMeta(this, value)
    }

/**
 * The total duration of the track queue in milliseconds.
 */
val BlockingDeque<Track>.duration: Long
    get() =
        // Streams don't have a valid time.
        filter { it.isSeekable }
            .sumOf { it.length.inWholeMilliseconds }

/**
 * Returns this duration in human-readable time string.
 */
val Duration.humanReadableTime: String
    get() = inWholeMilliseconds.humanReadableTime

/**
 * Assuming this long is in milliseconds, turn it into a readable time format.
 */
val Long.humanReadableTime: String
    get() {
        return if (this < 3600000) {
            String.format(
                "%02d:%02d",
                this.milliseconds.inWholeMinutes,
                this.milliseconds.inWholeSeconds % 1.minutes.inWholeSeconds
            )
        } else {
            String.format(
                "%02d:%02d:%02d",
                this.milliseconds.inWholeHours,
                this.milliseconds.inWholeMinutes % 1.hours.inWholeMinutes,
                this.milliseconds.inWholeSeconds % 1.minutes.inWholeSeconds
            )
        }
    }
