package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.stop() {
    ephemeralSlashCommand {
        name = "command.stop"
        description = "command.stop.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            val skipped = player.tracks.size

            player.clear()
            player.stop()

            log.info {
                message = "Player stopped"
                context = mapOf(
                    "user" to user.id
                )
            }

            if (skipped > 0) {
                respond { content = translate("response.stop.multiple", arrayOf(skipped)) }
            } else if (player.playing) {
                respond { content = translate("response.stop.success") }
            } else {
                respond { content = translate("response.stop.failure") }
            }
        }
    }
}