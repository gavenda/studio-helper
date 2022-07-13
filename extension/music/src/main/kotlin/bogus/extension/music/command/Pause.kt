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
        check {
            anyGuild()
        }
        action {
            if (player.paused) {
                respond {
                    content = translate("pause.response.failure")
                }
            } else {
                player.pause()

                log.info {
                    message = "Player paused"
                    context = mapOf(
                        "user" to user.id
                    )
                }

                respond {
                    content = translate("pause.response.success")
                }
            }
        }
    }
}