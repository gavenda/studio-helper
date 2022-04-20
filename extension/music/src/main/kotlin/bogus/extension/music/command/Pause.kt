package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.pause() {
    ephemeralSlashCommand {
        name = "pause"
        description = "pause.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            if (player.paused) {
                respond {
                    content = translate("pause.response.failure")
                }
            } else {
                player.pause()
                log.info { """msg="Paused player" user=${user.id}""" }
                respond {
                    content = translate("pause.response.success")
                }
            }
        }
    }
}