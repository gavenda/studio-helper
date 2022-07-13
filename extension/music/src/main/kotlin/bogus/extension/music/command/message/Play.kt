package bogus.extension.music.command.message

import bogus.checks.limit
import bogus.extension.music.*
import bogus.extension.music.checks.inVoiceChannel
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralMessageCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlin.time.Duration.Companion.minutes

suspend fun MusicExtension.playLater() {
    ephemeralMessageCommand {
        name = "command.play.later.message-command"
        check {
            anyGuild()
            inVoiceChannel()
        }
        action {
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
        name = "command.play.next.message-command"
        check {
            anyGuild()
            inVoiceChannel()
            limit(PLAY_NEXT_LIMIT, 5.minutes)
        }
        action {
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
        name = "command.play.now.message-command"
        check {
            anyGuild()
            inVoiceChannel()
            limit(PLAY_NOW_LIMIT, 5.minutes)
        }
        action {
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