package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.event.createLeaveMessage
import bogus.extension.administration.event.createWelcomeMessage
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun AdministrationExtension.welcomeMessage() {
    ephemeralSlashCommand {
        name = "welcome-message"
        description = "welcome-message.description"
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
