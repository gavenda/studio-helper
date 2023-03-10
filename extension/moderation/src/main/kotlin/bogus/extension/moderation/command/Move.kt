package bogus.extension.moderation.command

import bogus.extension.moderation.ModerationExtension

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.VoiceChannel
import mu.KotlinLogging

suspend fun ModerationExtension.move() {
    val log = KotlinLogging.logger { }

    ephemeralSlashCommand(::MoveArgs) {
        name = "move"
        description = "Move users from a voice channel to another."
        check {
            anyGuild()
            hasPermission(Permission.MoveMembers)
        }
        requireBotPermissions(Permission.MoveMembers)
        action {
            val fromVoice = arguments.from.asChannelOf<VoiceChannel>()
            val toVoice = arguments.to.asChannelOf<VoiceChannel>()

            fromVoice.voiceStates.collect {
                val member = it.getMemberOrNull() ?: return@collect
                member.edit { voiceChannelId = toVoice.id }
            }

            log.info { "Moved voice channel users" }

            respond {
                content = "Moved users from ${fromVoice.mention} to ${toVoice.mention}."
            }
        }
    }
}

private class MoveArgs : Arguments() {
    val from by channel {
        name = "from"
        description = "The voice channel to move users from."
        requireChannelType(ChannelType.GuildVoice)
    }
    val to by channel {
        name = "to"
        description = "The voice channel to move users to."
        requireChannelType(ChannelType.GuildVoice)
    }
}