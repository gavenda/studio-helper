package bogus.extension.information.command

import bogus.extension.information.DATE_FORMATTER
import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.toJavaInstant


suspend fun EphemeralSlashCommand<*>.role() {
    ephemeralSubCommand(::RoleArgs) {
        name = "command.role"
        description = "command.role.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            respond {
                embed {
                    author {
                        name = "Role Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    color = Color(InformationExtension.EMBED_COLOR)

                    field {
                        name = translate("response.info.field.name")
                        value = arguments.role.data.name
                    }

                    field {
                        name = "Date Created"
                        value = DATE_FORMATTER.format(channel.id.timestamp.toJavaInstant())
                    }
                }
            }
        }
    }
}

private class RoleArgs : Arguments() {
    val role by role {
        name = "command.role.args.role"
        description = "command.role.args.role.description"
    }
}