package bogus.extension.music.command

import bogus.checks.limit
import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.extension.music.*
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.checks.inVoiceChannel
import bogus.util.LRUCache
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
import kotlin.time.Duration.Companion.minutes

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

private suspend fun EphemeralSlashCommand<*>.next() {
    ephemeralSubCommand(::PlayArgs) {
        name = "next"
        description = "play.next.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
            limit(PLAY_NEXT_LIMIT, 5.minutes)
        }
        action(Dispatchers.IO) {
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

private suspend fun EphemeralSlashCommand<*>.now() {
    ephemeralSubCommand(::PlayArgs) {
        name = "now"
        description = "play.now.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
            limit(PLAY_NOW_LIMIT, 5.minutes)
        }
        action(Dispatchers.IO) {
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

internal class PlayArgs : KoinComponent, Arguments() {

    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    private val tp by inject<TranslationsProvider>()
    val query by string {
        name = "query"
        description = tp.translate(
            key = "play.args.query.description",
            bundleName = TRANSLATION_BUNDLE
        )

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
                        // Check cache
                        val cacheLookup = cache[input]
                        val delta = (AUTOCOMPLETE_ITEMS_LIMIT - fileResult.size).coerceAtLeast(0)

                        if (cacheLookup != null) {
                            cacheLookup.take(delta).forEach {
                                choice(it, it)
                            }
                        } else {
                            val youtubeResult = youtubeQuery(input)
                            youtubeResult.take(delta)
                                .apply { cache[input] = this }
                                .forEach {
                                    choice(it, it)
                                }
                        }
                    }
                }
            }
        }
    }
}