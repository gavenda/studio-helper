package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.event.createLeaveMessage
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission

suspend fun AdministrationExtension.leaveMessage() {
    ephemeralSlashCommand {
        name = "command.leave-message"
        description = "command.leave-message.description"
        requirePermission(Permission.Administrator)
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            respond {
                content = createLeaveMessage(user, guild)
            }
        }
    }
}
