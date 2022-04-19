package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.Dispatchers

suspend fun MusicExtension.resume() {
    ephemeralSlashCommand {
        name = "resume"
        description = "resume.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            if (player.paused.not()) {
                respond {
                    content = translate("resume.response.failure")
                }
            } else {
                player.resume()
                log.info { """msg="Resumed player" user=${user.id}""" }
                respond {
                    content = translate("resume.response.success")
                }
            }
        }
    }
}