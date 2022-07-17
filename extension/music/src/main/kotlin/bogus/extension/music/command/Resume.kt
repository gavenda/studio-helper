package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.resume() {
    ephemeralSlashCommand {
        name = "command.resume"
        description = "command.resume.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            if (player.paused.not()) {
                respond {
                    content = translate("resume.response.failure")
                }
            } else {
                player.resume()

                log.info {
                    message = "Player resumed"
                    context = mapOf(
                        "user" to user.id
                    )
                }

                respond {
                    content = translate("resume.response.success")
                }
            }
        }
    }
}