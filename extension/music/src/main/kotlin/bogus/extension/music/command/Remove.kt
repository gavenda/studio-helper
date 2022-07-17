package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import bogus.extension.music.player.MusicTrack
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject

suspend fun MusicExtension.remove() {
    ephemeralSlashCommand(::RemoveArgs) {
        name = "command.remove"
        description = "command.remove.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            if (arguments.to == null) {
                val i1 = (arguments.from - 1).coerceAtLeast(0)
                val i2 = i1 + 1

                respond {
                    content = removedMessage(player.remove(i1, i2))
                }
            } else if (arguments.to != null) {
                val i1 = (arguments.from - 1).coerceAtLeast(0)
                val i2 = arguments.to!!.coerceAtLeast(0)

                respond {
                    content = removedMessage(player.remove(i1, i2))
                }
            } else {
                respond {
                    content = translate("remove.response.missing-from")
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommandContext<*>.removedMessage(skipped: List<MusicTrack>): String =
    when (skipped.size) {
        0 -> translate("remove.response.nothing")
        1 -> translate("remove.response.single", arrayOf(skipped.first().title))
        else -> translate("remove.response.multiple", arrayOf(skipped.size))
    }

private class RemoveArgs : KordExKoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()

    val from by int {
        name = "command.remove.args.from"
        description = "command.remove.args.from.description"
    }

    val to by optionalInt {
        name = "command.remove.args.to"
        description = "command.remove.args.to.description"
    }
}