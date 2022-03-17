package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createCharacterEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.paginator.respondingStandardPaginator
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

suspend fun AniListExtension.character() {
    publicSlashCommand(::CharacterArgs) {
        name = "character"
        description = "Looks up the name of an anime/manga character."
        action(Dispatchers.IO) {
            findCharacter(arguments.query)
        }
    }

    publicMessageCommand {
        name = "Search Character"
        action(Dispatchers.IO) {
            findCharacter(targetMessages.first().content)
        }
    }
}

private suspend fun ApplicationCommandContext.findCharacter(query: String) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()
    val characters = aniList.findCharacter(query)

    log.info { "Looking up character [ query = $query, userId = ${user.id} ]" }

    if (characters == null || characters.isEmpty()) {
        respond {
            content = translate("character.error.noMatchingCharacter")
        }
        return
    }

    val paginator = respondingStandardPaginator {
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
    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime/manga character."

        autoComplete {
            if (!focusedOption.focused) return@autoComplete
            val typed = focusedOption.value

            suggestString {
                aniList.findCharacterNames(typed)
                    .take(25)
                    .forEach { characterName ->
                        choice(characterName.abbreviate(80), characterName.abbreviate(80))
                    }
            }
        }
    }
}