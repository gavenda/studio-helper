package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.skip() {
    ephemeralSlashCommand {
        name = "skip"
        description = "skip.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val audioTrack = guild.player.skip()

            log.info {
                message = "Music skipped"
                context = mapOf(
                    "user" to user.id
                )
            }

            if (audioTrack != null) {
                respond { content = translate("skip.response.skipped", arrayOf(audioTrack.title)) }
            } else {
                respond { content = translate("skip.response.nothing") }
            }
        }
    }
}