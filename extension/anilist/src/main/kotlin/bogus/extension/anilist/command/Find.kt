package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.MediaType
import bogus.extension.anilist.sendMediaResult
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.inject

suspend fun AniListExtension.find() {
    publicSlashCommand(::FindArgs) {
        name = "command.find"
        description = "command.find.description"
        action {
            findMedia(arguments.query)
        }
    }

    publicSlashCommand(::FindAnimeArgs) {
        name = "command.find.anime"
        description = "command.find.anime.description"
        action {
            findMedia(arguments.query, MediaType.ANIME)
        }
    }

    publicSlashCommand(::FindMangaArgs) {
        name = "command.find.manga"
        description = "command.find.manga.description"
        action {
            findMedia(arguments.query, MediaType.MANGA)
        }
    }
}

suspend fun ApplicationCommandContext.findMedia(
    query: String,
    type: MediaType? = null
) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()

    log.info {
        message = "Looking up media"
        context = mapOf(
            "query" to query,
            "userId" to user.id
        )
    }

    val hentai = true
    val media = aniList.findMedia(query, type, hentai)

    if (media == null || media.isEmpty()) {
        respond {
            content = translate("find.error.no-matching-media")
        }
        return
    }

    sendMediaResult(guild, media)
}

private class FindArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "command.find.args.query"
        description = "command.find.args.query.description"
        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                aniList.findMediaTitlesAsString(input)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}

private class FindAnimeArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "command.find.args.query"
        description = "command.find.anime.args.query.description"

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                aniList.findMediaTitlesAsString(input, MediaType.ANIME)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}

private class FindMangaArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "command.find.args.query"
        description = "command.find.manga.args.query.description"

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                aniList.findMediaTitlesAsString(input, MediaType.MANGA)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}