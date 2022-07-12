package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.MediaType
import bogus.extension.anilist.sendMediaResult
import bogus.util.LRUCache
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.inject

suspend fun AniListExtension.find() {
    publicSlashCommand(::FindArgs) {
        name = "find"
        description = "Looks up the name of the anime/manga."
        action {
            findMedia(arguments.query)
        }
    }

    publicSlashCommand(::FindAnimeArgs) {
        name = "anime"
        description = "Looks up the name of the anime."
        action {
            findMedia(arguments.query, MediaType.ANIME)
        }
    }

    publicSlashCommand(::FindMangaArgs) {
        name = "manga"
        description = "Looks up the name of the manga."
        action {
            findMedia(arguments.query, MediaType.MANGA)
        }
    }

    publicMessageCommand {
        name = "Search Trash"
        action {
            findMedia(targetMessages.first().content)
        }
    }
}

private suspend fun ApplicationCommandContext.findMedia(
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
            content = translate("find.error.noMatchingMedia")
        }
        return
    }

    sendMediaResult(guild, media)
}

private class FindArgs : KordExKoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "query"
        description = "Name of the anime/manga."
        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }
                } else {
                    aniList.findMediaTitlesAsString(input)
                        .map { it.abbreviate(80) }
                        .apply { cache[input] = this }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}

private class FindAnimeArgs : KordExKoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "query"
        description = "Name of the anime."

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }

                } else {
                    aniList.findMediaTitlesAsString(input, MediaType.ANIME)
                        .map { it.abbreviate(80) }
                        .apply { cache[input] = this }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}

private class FindMangaArgs : KordExKoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "query"
        description = "Name of the manga."

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }
                } else {
                    aniList.findMediaTitlesAsString(input, MediaType.MANGA)
                        .map { it.abbreviate(80) }
                        .apply { cache[input] = this }
                        .forEach { choice(it, it) }

                }
            }
        }
    }
}