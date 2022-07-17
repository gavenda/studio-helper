package bogus.extension.information.command

import bogus.extension.information.DATE_FORMATTER
import bogus.extension.information.InformationExtension.Companion.EMBED_COLOR
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.optional.value
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.toJavaInstant

suspend fun EphemeralSlashCommand<*>.channel() {
    ephemeralSubCommand(::ChannelArgs) {
        name = "command.channel"
        description = "command.channel.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            respond {
                embed {
                    author {
                        name = "Channel Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    color = Color(EMBED_COLOR)

                    arguments.channel.data.name.value?.let {
                        field {
                            name = translate("response.info.field.name")
                            value = it
                        }
                    }

                    field {
                        name = translate("response.info.field.date-created")
                        value = DATE_FORMATTER.format(arguments.channel.id.timestamp.toJavaInstant())
                    }

                    if (arguments.channel.type == ChannelType.GuildText) {
                        val messageChannel = arguments.channel.asChannelOf<TextChannel>()
                        messageChannel.data.messageCount.value?.let {
                            field {
                                name = "Message Count"
                                value = it.toString()
                            }
                        }
                    }

                    if (arguments.channel.type == ChannelType.GuildVoice) {
                        val voiceChannel = arguments.channel.asChannelOf<VoiceChannel>()
                        voiceChannel.data.bitrate.value?.let {
                            field {
                                name = "Bitrate"
                                value = "${it / 1000} kbps"
                            }
                        }

                        voiceChannel.data.rtcRegion.value?.let {
                            field {
                                name = "Region"
                                value = it
                            }
                        }
                    }

                    color = Color(EMBED_COLOR)
                }
            }
        }
    }
}

private class ChannelArgs : Arguments() {
    val channel by channel {
        name = "command.channel.args.channel"
        description = "command.channel.args.channel.description"
    }
}