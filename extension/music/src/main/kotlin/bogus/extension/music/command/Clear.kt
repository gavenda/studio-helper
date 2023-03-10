package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.clear() {
    ephemeralSlashCommand {
        name = "command.clear"
        allowInDms = false
        description = "command.clear.description"

        group("command.clear.effect") {
            description = "command.clear.effect.description"

            filter()
            equalizer()
        }

        queue()
    }
}

private suspend fun EphemeralSlashCommand<*, *>.queue() {
    ephemeralSubCommand {
        name = "command.clear.queue"
        description = "command.clear.queue.description"
        check {
            anyGuild()
        }
        action {
            if (player.tracks.isEmpty()) {
                respond {
                    content = translate("response.clear.queue.empty")
                }
            } else {
                player.clear()

                log.info { "Cleared queue" }

                respond {
                    content = translate("response.clear.queue")
                }
            }
        }
    }
}

private suspend fun SlashGroup.filter() {
    ephemeralSubCommand {
        name = "command.clear.effect.filter"
        description = "command.clear.effect.filter.description"
        check {
            anyGuild()
        }
        action {
            player.effects.clearFilter()

            log.info { "Cleared filters" }

            respond {
                content = translate("response.clear.filter")
            }
        }
    }
}

private suspend fun SlashGroup.equalizer() {
    ephemeralSubCommand {
        name = "command.clear.effect.equalizer"
        description = "command.clear.effect.equalizer.description"
        check {
            anyGuild()
        }
        action {
            player.effects.clearEqualizer()

            log.info { "Cleared equalizer" }

            respond {
                content = translate("response.clear.equalizer")
            }
        }
    }
}