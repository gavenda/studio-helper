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
        name = "clear"
        description = "clear.description"

        group("effect") {
            description = "clear.effect.description"

            filter()
            equalizer()
        }

        queue()
    }
}

private suspend fun EphemeralSlashCommand<*>.queue() {
    ephemeralSubCommand {
        name = "queue"
        description = "clear.queue.description"
        check {
            anyGuild()
        }
        action {
            if (player.tracks.isEmpty()) {
                respond {
                    content = translate("clear.queue.response.empty")
                }
            } else {
                player.clear()

                log.info {
                    message = "Cleared queue"
                    context = mapOf(
                        "user" to user.id
                    )
                }

                respond {
                    content = translate("clear.queue.response.cleared")
                }
            }
        }
    }
}

private suspend fun SlashGroup.filter() {
    ephemeralSubCommand {
        name = "filter"
        description = "clear.effect.filter.description"
        check {
            anyGuild()
        }
        action {
            player.effects.clearFilter()

            log.info {
                message = "Cleared filters"
                context = mapOf(
                    "user" to user.id
                )
            }

            respond {
                content = translate("clear.filter.response")
            }
        }
    }
}

private suspend fun SlashGroup.equalizer() {
    ephemeralSubCommand {
        name = "equalizer"
        description = "clear.effect.equalizer.description"
        check {
            anyGuild()
        }
        action {
            player.effects.clearEqualizer()

            log.info {
                message = "Cleared equalizer"
                context = mapOf(
                    "user" to user.id
                )
            }

            respond {
                content = translate("clear.equalizer.response")
            }
        }
    }
}