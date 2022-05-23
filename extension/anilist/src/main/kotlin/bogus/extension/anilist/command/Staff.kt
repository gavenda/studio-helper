package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createStaffEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.paginator.respondingStandardPaginator
import bogus.util.LRUCache
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.inject

suspend fun AniListExtension.staff() {
    publicSlashCommand(::StaffArgs) {
        name = "staff"
        description = "Looks up the name of an anime/manga staff."
        action {
            findStaff(arguments.query)
        }
    }

    publicMessageCommand {
        name = "Search Staff"
        action {
            findStaff(targetMessages.first().content)
        }
    }
}

private suspend fun ApplicationCommandContext.findStaff(query: String) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()
    val staffs = aniList.findStaff(query)

    log.info(
        msg = "Looking up staff",
        context = mapOf(
            "query" to query,
            "userId" to user.id
        )
    )

    if (staffs == null || staffs.isEmpty()) {
        respond {
            content = translate("staff.error.noMatchingStaff")
        }
        return
    }
    val paginator = respondingStandardPaginator(linkLabel = translate("link.label")) {
        timeoutSeconds = PAGINATOR_TIMEOUT
        staffs.forEach { staff ->
            page {
                apply(createStaffEmbed(staff))
            }
        }
    }

    paginator.send()
}

internal class StaffArgs : KordExKoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val query by string {
        name = "query"
        description = "Name of the anime/manga staff."
        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }
                } else {
                    aniList.findStaffNames(input)
                        .map { it.abbreviate(80) }
                        .apply { cache[input] = this }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}
