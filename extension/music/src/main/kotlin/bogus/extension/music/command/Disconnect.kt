package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.disconnect() {
    ephemeralSlashCommand {
        name = "command.disconnect"
        description = "command.disconnect.description"
        allowInDms = false
        check {
            anyGuild()
        }
        action {
            player.stop()
            player.disconnect()

            log.info {
                message = "Disconnected from voice"
                context = mapOf(
                    "user" to user.id
                )
            }

            respond {
                content = translate("response.disconnect")
            }
        }
    }
}