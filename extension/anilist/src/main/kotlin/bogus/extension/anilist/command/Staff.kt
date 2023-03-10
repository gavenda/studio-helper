package bogus.extension.anilist.command

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.paginator.respondingStandardPaginator
import bogus.util.abbreviate

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

suspend fun AniListExtension.staff() {
    publicSlashCommand(::StaffArgs) {
        name = "command.staff"
        description = "command.staff.description"
        action {
            findStaff(arguments.query)
        }
    }
}

suspend fun ApplicationCommandContext.findStaff(query: String) {
    if (this !is PublicInteractionContext) return

    val aniList by inject<AniList>()
    val log = KotlinLogging.logger { }
    val staffs = aniList.findStaff(query)

    log.info { "Looking up staff [ query = $query, userId = ${user.id} ]" }

    if (staffs == null || staffs.isEmpty()) {
        respond {
            content = translate("staff.error.no-matching-staff")
        }
        return
    }
    val paginator = respondingStandardPaginator(linkLabel = translate("find.link.label")) {
        timeoutSeconds = PAGINATOR_TIMEOUT
        staffs.forEach { staff ->
            page {
                apply(staff.createEmbed())
            }
        }
    }

    paginator.send()
}

private class StaffArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val query by coalescingString {
        name = "command.staff.args.query"
        description = "command.staff.args.query.description"
        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                aniList.findStaffNames(input)
                    .take(AUTOCOMPLETE_ITEMS_LIMIT)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}
