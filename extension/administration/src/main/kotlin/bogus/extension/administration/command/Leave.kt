package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.event.createLeaveMessage
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun AdministrationExtension.leaveMessage() {
    ephemeralSlashCommand {
        name = "leave-message"
        description = "leave-message.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild?.fetchGuild() ?: return@action
            respond {
                content = createLeaveMessage(user, guild)
            }
        }
    }
}
