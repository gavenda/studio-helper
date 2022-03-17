package bogus.extension.music.command

import bogus.extension.music.*
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.check.inVoiceChannel
import bogus.extension.music.youtubeQuery
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

suspend fun MusicExtension.play() {
    ephemeralSlashCommand {
        name = "play"
        description = "play.description"

        later()
        next()
        now()
    }
}

private suspend fun EphemeralSlashCommand<*>.later() {
    ephemeralSubCommand(::PlayArgs) {
        name = "later"
        description = "play.later.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(arguments.query)
            val response = Jukebox.playLater(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    identifiers = identifiers,
                    guild = guild,
                    mention = user.mention,
                    userId = user.id,
                    locale = getLocale()
                )
            )

            if (response.isNotBlank()) {
                respond { content = response }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.next() {
    ephemeralSubCommand(::PlayArgs) {
        name = "next"
        description = "play.next.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(arguments.query)
            val response = Jukebox.playNext(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    identifiers = identifiers,
                    guild = guild,
                    mention = user.mention,
                    userId = user.id,
                    locale = getLocale()
                )
            )

            if (response.isNotBlank()) {
                respond { content = response }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.now() {
    ephemeralSubCommand(::PlayArgs) {
        name = "now"
        description = "play.now.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(arguments.query)
            val response = Jukebox.playNow(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    identifiers = identifiers,
                    guild = guild,
                    mention = user.mention,
                    userId = user.id,
                    locale = getLocale()
                )
            )

            if (response.isNotBlank()) {
                respond { content = response }
            }
        }
    }
}

internal class PlayArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    val query by string {
        name = "query"
        description = tp.translate(
            key = "play.args.query.description",
            bundleName = TRANSLATION_BUNDLE
        )

        autoComplete {
            if (!focusedOption.focused) return@autoComplete

            val input = focusedOption.value
            val youtubeResult = youtubeQuery(input)

            suggestString {
                youtubeResult.take(25).forEach { choice(it, it) }
            }
        }
    }
}