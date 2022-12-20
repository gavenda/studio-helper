package bogus.extension.music.command

import bogus.checks.limit
import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.extension.music.*
import bogus.extension.music.checks.inVoiceChannel
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import kotlin.time.Duration.Companion.minutes

suspend fun MusicExtension.play() {
    ephemeralSlashCommand {
        name = "command.play"
        description = "command.play.description"
        allowInDms = false

        later()
        next()
        now()
    }
}

private suspend fun EphemeralSlashCommand<*, *>.later() {
    ephemeralSubCommand(::PlayArgs) {
        name = "command.play.later"
        description = "command.play.later.description"
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
                    parseResult = IdentifierParser.toIdentifiers(arguments.query),
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

private suspend fun EphemeralSlashCommand<*, *>.next() {
    ephemeralSubCommand(::PlayArgs) {
        name = "command.play.next"
        description = "command.play.next.description"
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
                    parseResult = IdentifierParser.toIdentifiers(arguments.query),
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

private suspend fun EphemeralSlashCommand<*, *>.now() {
    ephemeralSubCommand(::PlayArgs) {
        name = "command.play.now"
        description = "command.play.now.description"
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
                    parseResult = IdentifierParser.toIdentifiers(arguments.query),
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

private class PlayArgs : Arguments() {
    val query by coalescingString {
        name = "command.play.args.query"
        description = "command.play.args.query.description"

        autoComplete {
            val input = focusedOption.value
            val fileList = IdentifierParser.listFiles()

            if (input.isBlank()) {
                suggestString {
                    fileList.take(AUTOCOMPLETE_ITEMS_LIMIT).forEach {
                        choice(it, "local:$it")
                    }
                }
            } else {
                suggestString {
                    val fileResult = fileList
                        .filter { it.startsWith(input, ignoreCase = true) }
                        .toList()

                    fileResult.take(AUTOCOMPLETE_ITEMS_LIMIT).forEach {
                        choice(it, "local:$it")
                    }

                    if (fileResult.size < AUTOCOMPLETE_ITEMS_LIMIT) {
                        val delta = (AUTOCOMPLETE_ITEMS_LIMIT - fileResult.size).coerceAtLeast(0)
                        val youtubeResult = YT.query(input)
                        youtubeResult.take(delta)
                            .forEach {
                                choice(it, it)
                            }
                    }
                }
            }
        }
    }
}