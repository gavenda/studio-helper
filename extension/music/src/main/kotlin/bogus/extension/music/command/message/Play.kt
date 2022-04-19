package bogus.extension.music.command.message

import bogus.checks.limit
import bogus.extension.music.IdentifierParser
import bogus.extension.music.Jukebox
import bogus.extension.music.MusicExtension
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.checks.inVoiceChannel
import bogus.extension.music.respondChoices
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.minutes

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
            val response = Jukebox.playLater(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    respondMultiple = { choices, select -> respondChoices(choices, select) },
                    parseResult = IdentifierParser.toIdentifiers(targetMessages.first().content),
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
            limit(5.minutes)
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val response = Jukebox.playNext(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    respondMultiple = { choices, select -> respondChoices(choices, select) },
                    parseResult = IdentifierParser.toIdentifiers(targetMessages.first().content),
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
            limit(5.minutes)
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val response = Jukebox.playNow(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    respondMultiple = { choices, select -> respondChoices(choices, select) },
                    parseResult = IdentifierParser.toIdentifiers(targetMessages.first().content),
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