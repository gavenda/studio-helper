package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.MediaType
import bogus.extension.anilist.sendMediaResult
import bogus.util.LRUCache
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
            findMedia(arguments.query, MediaType.ANIME)
        }
    }

    publicSlashCommand(::FindMangaArgs) {
        name = "manga"
        description = "Looks up the name of the manga."
        action(Dispatchers.IO) {
            findMedia(arguments.query, MediaType.MANGA)
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
    type: MediaType? = null
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
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime/manga."
        autoComplete {
            val input = focusedOption.value
            val cacheLookup = cache[input]

            if (cacheLookup != null) {
                suggestString {
                    cacheLookup.forEach { choice(it, it) }
                }
            } else {
                suggestString {
                    aniList.findMediaTitles(input)
                        .apply { cache[input] = this }
                        .map { it.abbreviate(80) }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}

internal class FindAnimeArgs : KoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime."

        autoComplete {
            val input = focusedOption.value
            val cacheLookup = cache[input]

            if (cacheLookup != null) {
                suggestString {
                    cacheLookup.forEach { choice(it, it) }
                }
            } else {
                suggestString {
                    aniList.findMediaTitles(input, MediaType.ANIME)
                        .apply { cache[input] = this }
                        .map { it.abbreviate(80) }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}

internal class FindMangaArgs : KoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the manga."

        autoComplete {
            val input = focusedOption.value
            val cacheLookup = cache[input]

            if (cacheLookup != null) {
                suggestString {
                    cacheLookup.forEach { choice(it, it) }
                }
            } else {
                suggestString {
                    aniList.findMediaTitles(input, MediaType.MANGA)
                        .apply { cache[input] = this }
                        .map { it.abbreviate(80) }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}