package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.shuffle() {
    ephemeralSlashCommand {
        name = "command.shuffle"
        description = "command.shuffle.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            if (player.playing) {
                player.shuffle()

                log.info {
                    message = "Playlist shuffled"
                    context = mapOf(
                        "user" to user.id
                    )
                }

                respond {
                    content = translate("shuffle.response.success")
                }
            } else {
                respond {
                    content = translate("shuffle.response.failure")
                }
            }
        }
    }
}