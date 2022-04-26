package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.shuffle() {
    ephemeralSlashCommand {
        name = "shuffle"
        description = "shuffle.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            if (player.playing) {
                player.shuffle()

                log.info(
                    msg = "Playlist shuffled",
                    context = mapOf(
                        "user" to user.id
                    )
                )

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