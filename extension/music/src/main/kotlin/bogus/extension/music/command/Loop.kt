package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.loop() {
    ephemeralSlashCommand {
        name = "command.loop"
        description = "command.loop.description"
        allowInDms = false

        single()
        all()
    }
}

private suspend fun EphemeralSlashCommand<*, *>.single() {
    ephemeralSubCommand {
        name = "command.loop.single"
        description = "command.loop.single.description"
        check {
            anyGuild()
        }
        action {
            player.toggleLoop()

            log.info { "Toggled loop single" }

            if (player.looped) {
                respond {
                    content = translate("response.loop.single.start")
                }
            } else {
                respond {
                    content = translate("response.loop.single.stop")
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.all() {
    ephemeralSubCommand {
        name = "command.loop.all"
        description = "command.loop.all.description"
        check {
            anyGuild()
        }
        action {
            player.toggleLoopAll()
            log.info { "Toggled loop all" }
            if (player.loopedAll) {
                respond {
                    content = translate("response.loop.all.start")
                }
            } else {
                respond {
                    content = translate("response.loop.all.stop")
                }
            }
        }
    }
}