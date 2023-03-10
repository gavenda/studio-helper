package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.pause() {
    ephemeralSlashCommand {
        name = "command.pause"
        description = "command.pause.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            if (player.paused) {
                respond {
                    content = translate("response.pause.failure")
                }
            } else {
                player.pause()

                log.info { "Player paused" }

                respond {
                    content = translate("response.pause.success")
                }
            }
        }
    }
}