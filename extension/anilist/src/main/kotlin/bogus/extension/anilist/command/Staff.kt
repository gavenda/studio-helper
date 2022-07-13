package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.AniListExtension.log
import bogus.extension.anilist.PAGINATOR_TIMEOUT
import bogus.extension.anilist.embed.createStaffEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.paginator.respondingStandardPaginator
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

suspend fun AniListExtension.staff() {
    publicSlashCommand(::StaffArgs) {
        name = "command.staff"
        description = "command.staff.description"
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

    log.info {
        message = "Looking up staff"
        context = mapOf(
            "query" to query,
            "userId" to user.id
        )
    }

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
                apply(createStaffEmbed(staff))
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
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}
