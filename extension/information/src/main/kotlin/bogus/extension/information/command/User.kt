package bogus.extension.information.command

import bogus.extension.information.DATE_FORMATTER
import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.toJavaInstant


suspend fun EphemeralSlashCommand<*>.user() {
    ephemeralSubCommand(::UserArgs) {
        name = "command.user"
        description = "command.user.description"
        check {
            anyGuild()
        }
        action {
            respond {
                embed {
                    author {
                        name = "User Information"
                        icon = arguments.user.avatar?.cdnUrl?.toUrl()
                    }
                    color = Color(InformationExtension.EMBED_COLOR)

                    field {
                        name = translate("response.info.field.name")
                        value = arguments.user.username
                    }

                    field {
                        name = translate("response.info.field.date-created")
                        value = DATE_FORMATTER.format(arguments.user.id.timestamp.toJavaInstant())
                    }
                }
            }
        }
    }
}

private class UserArgs : Arguments() {
    val user by user {
        name = "command.user.args.user"
        description = "command.user.args.user.description"
    }
}