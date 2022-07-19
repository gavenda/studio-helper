package bogus.extension.music

import bogus.extension.music.player.MusicPlayer
import bogus.extension.music.player.MusicTrack
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.application.message.EphemeralMessageCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.GuildBehavior
import java.util.concurrent.BlockingDeque
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Player instance.
 */
val ApplicationCommandContext.player: MusicPlayer
    get() {
        return guild?.let { Jukebox.playerFor(it.id) } ?: error("Missing guild")
    }

/**
 * Guild music player.
 */
val GuildBehavior.player: MusicPlayer
    get() {
        return Jukebox.playerFor(id)
    }

/**
 * The total duration of the track queue in milliseconds.
 */
val BlockingDeque<MusicTrack>.duration: Long
    get() {
        // Streams don't have a valid time.
        val result = filter { it.seekable }

        if (result.isNotEmpty()) {
            return result.sumOf { it.length.inWholeMilliseconds }
        } else {
            return 0L
        }
    }

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
        return if (this == Long.MIN_VALUE || this == Long.MAX_VALUE) {
            "-"
        } else if (this < 3600000) {
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

suspend fun EphemeralSlashCommandContext<*>.respondChoices(
    choices: List<MusicTrack>,
    select: suspend (MusicTrack) -> String
) {
    respond {
        components {
            ephemeralSelectMenu {
                content = translate("response.jukebox.choices")
                choices.forEachIndexed { idx, track ->
                    option(track.title.abbreviate(80), track.uri) {
                        emoji = if (idx == 0) EmojiPreferred else EmojiMusicNote
                    }
                }
                action {
                    val selectedTrack = selected.first()
                    val track = choices.first { it.uri == selectedTrack }

                    edit {
                        content = select(track)
                        components { removeAll() }
                    }
                }
            }
        }
    }
}

suspend fun EphemeralMessageCommandContext.respondChoices(
    choices: List<MusicTrack>,
    select: suspend (MusicTrack) -> String
) {
    respond {
        components {
            ephemeralSelectMenu {
                content = translate("response.jukebox.choices")
                choices.forEachIndexed { idx, track ->
                    option(track.title.abbreviate(80), track.uri) {
                        emoji = if (idx == 0) EmojiPreferred else EmojiMusicNote
                    }
                }
                action {
                    val selectedTrack = selected.first()
                    val track = choices.first { it.uri == selectedTrack }

                    edit {
                        content = select(track)
                        components { removeAll() }
                    }
                }
            }
        }
    }
}
