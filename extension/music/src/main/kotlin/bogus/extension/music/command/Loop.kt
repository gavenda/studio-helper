package bogus.extension.music.command

import bogus.extension.music.MusicEffects
import bogus.extension.music.MusicExtension
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.loop() {
    ephemeralSlashCommand {
        name = "loop"
        description = "loop.description"

        single()
        all()
    }
}

private suspend fun EphemeralSlashCommand<*>.single() {
    ephemeralSubCommand {
        name = "single"
        description = "loop.single.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            player.toggleLoop()

            log.info(
                msg = "Toggled loop single",
                context = mapOf(
                    "looped" to player.looped,
                    "user" to user.id
                )
            )

            if (player.looped) {
                respond {
                    content = translate("loop.response.single.start")
                }
            } else {
                respond {
                    content = translate("loop.response.single.stop")
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.all() {
    ephemeralSubCommand {
        name = "all"
        description = "loop.all.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            player.toggleLoopAll()
            log.info(
                msg = "Toggled loop all",
                context = mapOf(
                    "looped" to player.loopedAll,
                    "user" to user.id
                )
            )
            if (player.loopedAll) {
                respond {
                    content = translate("loop.response.all.start")
                }
            } else {
                respond {
                    content = translate("loop.response.all.stop")
                }
            }
        }
    }
}