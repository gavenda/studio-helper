package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.skip() {
    ephemeralSlashCommand {
        name = "command.skip"
        description = "command.skip.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val audioTrack = guild.player.skip()

            log.info { "Music skipped" }

            if (audioTrack != null) {
                respond { content = translate("response.skip.skipped", arrayOf(audioTrack.title)) }
            } else {
                respond { content = translate("response.skip.nothing") }
            }
        }
    }
}