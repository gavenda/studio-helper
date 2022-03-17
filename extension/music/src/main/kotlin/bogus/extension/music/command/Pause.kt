package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.Dispatchers

suspend fun MusicExtension.pause() {
    ephemeralSlashCommand {
        name = "pause"
        description = "pause.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
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