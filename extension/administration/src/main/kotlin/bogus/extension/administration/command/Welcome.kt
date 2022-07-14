package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.event.createWelcomeMessage
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission

suspend fun AdministrationExtension.welcomeMessage() {
    ephemeralSlashCommand {
        name = "command.welcome-message"
        description = "command.welcome-message.description"
        requirePermission(Permission.Administrator)
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            val member = member ?: return@action

            respond {
                content = createWelcomeMessage(member, guild)
            }
        }
    }
}
