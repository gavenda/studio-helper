package bogus.extension.information.command

import bogus.extension.information.InformationExtension.Companion.EMBED_COLOR
import bogus.util.selfAvatarUrl
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.emoji
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed

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