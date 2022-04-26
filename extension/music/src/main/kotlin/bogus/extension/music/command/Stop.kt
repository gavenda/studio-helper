package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.stop() {
    ephemeralSlashCommand {
        name = "stop"
        description = "stop.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val skipped = player.tracks.size

            player.clear()
            player.stop()

            log.info(
                msg = "Player stopped",
                context = mapOf(
                    "user" to user.id
                )
            )

            if (skipped > 0) {
                respond { content = translate("stop.response.multiple", arrayOf(skipped)) }
            } else if (player.playing) {
                respond { content = translate("stop.response.success") }
            } else {
                respond { content = translate("stop.response.failure") }
            }
        }
    }
}