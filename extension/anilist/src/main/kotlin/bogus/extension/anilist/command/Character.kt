package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createCharacterEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.paginator.respondingStandardPaginator
import bogus.util.LRUCache
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

suspend fun AniListExtension.character() {
    publicSlashCommand(::CharacterArgs) {
        name = "character"
        description = "Looks up the name of an anime/manga character."
        action {
            findCharacter(arguments.query)
        }
    }

    publicMessageCommand {
        name = "Search Character"
        action {
            findCharacter(targetMessages.first().content)
        }
    }
}

private suspend fun ApplicationCommandContext.findCharacter(query: String) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()
    val characters = aniList.findCharacter(query)

    log.info(
        msg = "Looking up character",
        context = mapOf(
            "query" to query,
            "userId" to user.id
        )
    )

    if (characters == null || characters.isEmpty()) {
        respond {
            content = translate("character.error.noMatchingCharacter")
        }
        return
    }

    val paginator = respondingStandardPaginator(linkLabel = translate("link.label")) {
        timeoutSeconds = PAGINATOR_TIMEOUT
        characters.forEach { character ->
            page {
                apply(createCharacterEmbed(character))
            }
        }
    }

    paginator.send()
}

internal class CharacterArgs : KoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime/manga character."

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }
                } else {
                    aniList.findCharacterNames(input)
                        .map { it.abbreviate(80) }
                        .apply { cache[input] = this }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}