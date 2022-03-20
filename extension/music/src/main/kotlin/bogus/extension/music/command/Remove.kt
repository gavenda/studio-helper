package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.TRANSLATION_BUNDLE
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.player
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import dev.schlaubi.lavakord.audio.player.Track
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

suspend fun MusicExtension.remove() {
    ephemeralSlashCommand(::RemoveArgs) {
        name = "remove"
        description = "remove.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
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

private suspend fun EphemeralSlashCommandContext<*>.removedMessage(skipped: List<Track>): String = when (skipped.size) {
    0 -> translate("remove.response.nothing")
    1 -> translate("remove.response.single", arrayOf(skipped.first().title))
    else -> translate("remove.response.multiple", arrayOf(skipped.size))
}

internal class RemoveArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()

    val from by int {
        name = "from"
        description = tp.translate(
            key = "remove.args.from",
            bundleName = TRANSLATION_BUNDLE
        )
    }

    val to by optionalInt {
        name = "to"
        description = tp.translate(
            key = "remove.args.to",
            bundleName = TRANSLATION_BUNDLE
        )
    }
}