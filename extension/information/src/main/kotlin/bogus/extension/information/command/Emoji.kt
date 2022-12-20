package bogus.extension.information.command

import bogus.extension.information.DATE_FORMATTER
import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.emoji
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.toJavaInstant


suspend fun EphemeralSlashCommand<*, *>.emoji() {
    ephemeralSubCommand(::EmojiArgs) {
        name = "command.emoji"
        description = "command.emoji.description"
        check {
            anyGuild()
        }
        action {
            respond {
                embed {
                    title = "Emoji Information"
                    color = Color(InformationExtension.EMBED_COLOR)

                    thumbnail {
                        url = arguments.emoji.image.cdnUrl.toUrl()
                    }

                    arguments.emoji.data.name?.let {
                        field {
                            name = translate("response.info.field.name")
                            value = ":$it:"
                        }
                    }

                    field {
                        name = translate("response.info.field.date-created")
                        value = DATE_FORMATTER.format(arguments.emoji.id.timestamp.toJavaInstant())
                    }

                    arguments.emoji.data.userId.value?.let {
                        field {
                            name = "Created By"
                            value = "<@${it}>"
                        }
                    }
                }
            }
        }
    }
}

private class EmojiArgs : Arguments() {
    val emoji by emoji {
        name = "command.emoji.args.emoji"
        description = "command.emoji.args.emoji.description"
    }
}