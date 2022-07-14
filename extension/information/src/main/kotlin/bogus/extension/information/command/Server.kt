package bogus.extension.information.command

import bogus.extension.information.InformationExtension
import bogus.util.selfAvatarUrl
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed


suspend fun EphemeralSlashCommand<*>.server() {
    ephemeralSubCommand {
        name = "command.server"
        description = "command.server.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            respond {
                embed {
                    author {
                        name = "Server Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    color = Color(InformationExtension.EMBED_COLOR)
                }
            }
        }
    }
}