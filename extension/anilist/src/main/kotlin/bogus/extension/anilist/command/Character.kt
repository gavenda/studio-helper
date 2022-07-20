package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.paginator.respondingStandardPaginator
import bogus.util.abbreviate
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import mu.KotlinLogging
import org.koin.core.component.inject

suspend fun AniListExtension.character() {
    publicSlashCommand(::CharacterArgs) {
        name = "command.character"
        description = "command.character.description"
        action {
            findCharacter(arguments.query)
        }
    }

}

suspend fun ApplicationCommandContext.findCharacter(query: String) {
    if (this !is PublicInteractionContext) return

    val log = KotlinLogging.logger { }.asFMTLogger()
    val aniList by inject<AniList>()
    val characters = aniList.findCharacter(query)

    log.info {
        message = "Looking up character"
        context = mapOf(
            "query" to query,
            "userId" to user.id
        )
    }

    if (characters == null || characters.isEmpty()) {
        respond {
            content = translate("character.error.no-matching-character")
        }
        return
    }

    val paginator = respondingStandardPaginator(linkLabel = translate("find.link.label")) {
        timeoutSeconds = PAGINATOR_TIMEOUT
        characters.forEach { character ->
            page {
                apply(character.createEmbed())
            }
        }
    }

    paginator.send()
}

private class CharacterArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "command.character.args.query"
        description = "command.character.args.query.description"

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                aniList.findCharacterNames(input)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}