package bogus.extension.music.command.message

import bogus.extension.music.IdentifierParser
import bogus.extension.music.Jukebox
import bogus.extension.music.MusicExtension
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.check.inVoiceChannel
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import kotlinx.coroutines.Dispatchers
import bogus.util.action
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.playLater() {
    ephemeralMessageCommand {
        name = "play.later.message-command"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(targetMessages.first().content)
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

suspend fun MusicExtension.playNext() {
    ephemeralMessageCommand {
        name = "play.next.message-command"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(targetMessages.first().content)
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

suspend fun MusicExtension.playNow() {
    ephemeralMessageCommand {
        name = "play.now.message-command"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val identifiers = IdentifierParser.toIdentifiers(targetMessages.first().content)
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