package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.Dispatchers

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
            hasDJRole()
        }
        action(Dispatchers.IO) {
            if (player.tracks.isEmpty()) {
                respond {
                    content = translate("clear.queue.response.empty")
                }
            } else {
                player.clear()
                log.info { """msg="Cleared queue" user=${user.id}""" }
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
            hasDJRole()
        }
        action(Dispatchers.IO) {
            player.effects.clearFilter()
            log.info { """msg="Cleared filters" user=${user.id}""" }
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
            hasDJRole()
        }
        action(Dispatchers.IO) {
            player.effects.clearEqualizer()
            log.info { """msg="Cleared equalizer" user=${user.id}""" }
            respond {
                content = translate("clear.equalizer.response")
            }
        }
    }
}