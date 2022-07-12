package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.GuildMessageChannel

suspend fun AdministrationExtension.broadcast() {
    ephemeralSlashCommand(::BroadcastArgs) {
        name = "broadcast"
        description = "broadcast.description"
        requirePermission(Permission.Administrator)
        action {
            val gmc = arguments.channel.asChannelOf<GuildMessageChannel>()

            gmc.createMessage {
                content = arguments.message
            }

            respond {
                content = "Message broadcast!"
            }
        }
    }
}

private class BroadcastArgs : Arguments() {
    val channel by channel {
        name = "channel"
        description = "channel.arguments.channel.description"
        requireSameGuild = true
        requireChannelType(ChannelType.GuildText)
        requireChannelType(ChannelType.PublicGuildThread)
        requireChannelType(ChannelType.PrivateThread)
        requireChannelType(ChannelType.PublicNewsThread)
    }
    val message by coalescingString {
        name = "message"
        description = "channel.arguments.message.description"
    }
}