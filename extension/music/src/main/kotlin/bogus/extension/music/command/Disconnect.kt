package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.link
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.disconnect() {
    ephemeralSlashCommand {
        name = "disconnect"
        description = "disconnect.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            player.stop()
            link.disconnectAudio()
            log.info { """msg="Disconnected singer" user=${user.id}""" }
            respond {
                content = translate("disconnect.response")
            }
        }
    }
}