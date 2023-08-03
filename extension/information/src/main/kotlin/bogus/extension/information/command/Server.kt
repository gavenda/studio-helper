package bogus.extension.information.command

import bogus.extension.information.DATE_FORMATTER
import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.count
import kotlinx.datetime.toJavaInstant


suspend fun EphemeralSlashCommand<*, *>.server() {
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
                    title = "Server Information"
                    color = Color(InformationExtension.EMBED_COLOR)

                    thumbnail {
                        url = guild.icon?.cdnUrl?.toUrl { format = Image.Format.WEBP } ?: ""
                    }

                    field {
                        name = translate("response.info.field.name")
                        value = guild.name
                    }

                    field {
                        name = translate("response.info.field.date-created")
                        value = DATE_FORMATTER.format(guild.id.timestamp.toJavaInstant())
                    }

                    field {
                        name = "Owner"
                        value = guild.owner.mention
                        inline = true
                    }

                    field {
                        name = "Emojis"
                        value = guild.emojiIds.size.toString()
                        inline = true
                    }

                    field {
                        name = "Stickers"
                        value = guild.stickers.count().toString()
                        inline = true
                    }

                    field {
                        name = "Roles"
                        value = guild.roleIds.size.toString()
                        inline = true
                    }

                    field {
                        name = "Channels"
                        value = guild.channelIds.size.toString()
                        inline = true
                    }

                    field {
                        name = "Members"
                        value = guild.memberCount.toString()
                        inline = true
                    }
                }
            }
        }
    }
}