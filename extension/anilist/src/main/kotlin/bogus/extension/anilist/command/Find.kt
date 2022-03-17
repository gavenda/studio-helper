package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.sendMediaResult
import bogus.util.abbreviate
import bogus.util.action
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

suspend fun AniListExtension.find() {
    publicSlashCommand(::FindArgs) {
        name = "find"
        description = "Looks up the name of the anime/manga."
        action(Dispatchers.IO) {
            findMedia(arguments.query)
        }
    }

    publicSlashCommand(::FindAnimeArgs) {
        name = "anime"
        description = "Looks up the name of the anime."
        action(Dispatchers.IO) {
            findMedia(arguments.query, bogus.extension.anilist.model.MediaType.ANIME)
        }
    }

    publicSlashCommand(::FindMangaArgs) {
        name = "manga"
        description = "Looks up the name of the manga."
        action(Dispatchers.IO) {
            findMedia(arguments.query, bogus.extension.anilist.model.MediaType.MANGA)
        }
    }

    publicMessageCommand {
        name = "Search Trash"
        action(Dispatchers.IO) {
            findMedia(targetMessages.first().content)
        }
    }
}

private suspend fun ApplicationCommandContext.findMedia(
    query: String,
    type: bogus.extension.anilist.model.MediaType? = null
) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()

    log.info { "Looking up media [ query = $query, userId = ${user.id} ]" }

    val hentai = true
    val media = aniList.findMedia(query, type, hentai)

    if (media == null || media.isEmpty()) {
        respond {
            content = translate("find.error.noMatchingMedia")
        }
        return
    }

    sendMediaResult(guild, media)
}

internal class FindArgs : KoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime/manga."
        autoComplete {
            if (!focusedOption.focused) return@autoComplete
            val typed = focusedOption.value

            suggestString {
                aniList.findMediaTitles(typed)
                    .take(25)
                    .forEach { media ->
                        choice(media.abbreviate(100), media)
                    }
            }
        }
    }
}

internal class FindAnimeArgs : KoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime."

        autoComplete {
            if (!focusedOption.focused) return@autoComplete
            val typed = focusedOption.value

            suggestString {
                aniList.findMediaTitles(typed, bogus.extension.anilist.model.MediaType.ANIME)
                    .take(25)
                    .forEach { media ->
                        choice(media.abbreviate(100), media)
                    }
            }
        }
    }
}

internal class FindMangaArgs : KoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the manga."

        autoComplete {
            if (!focusedOption.focused) return@autoComplete
            val typed = focusedOption.value

            suggestString {
                aniList.findMediaTitles(typed, bogus.extension.anilist.model.MediaType.MANGA).take(25)
                    .forEach { media ->
                        choice(media.abbreviate(80), media.abbreviate(80))
                    }
            }
        }
    }
}