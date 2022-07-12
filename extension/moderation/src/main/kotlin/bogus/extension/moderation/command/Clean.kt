package bogus.extension.moderation.command

import bogus.extension.moderation.ModerationExtension
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

suspend fun ModerationExtension.clean() {
    val log = KotlinLogging.logger { }
        .asFMTLogger()

    ephemeralSlashCommand(::CleanArgs) {
        name = "clean"
        description = "Cleans a message up to 100 messages."
        check {
            anyGuild()
            hasPermission(Permission.ManageMessages)
        }
        requireBotPermissions(Permission.ManageMessages)
        action {
            val lastMessageId = channel.messages
                .take(1)
                .first()
                .id

            val limit = arguments.amount

            log.info {
                message = "Cleaning messages"
                context = mapOf(
                    "amount" to arguments.amount,
                    "lastMessageId" to lastMessageId,
                    "reason" to arguments.reason
                )
            }

            val messages = channel.getMessagesBefore(lastMessageId, limit)
                .map { it.id }
                .toList()

            channel.asChannelOf<GuildMessageChannel>().bulkDelete(messages, arguments.reason)

            respond {
                content = "Cleaned ${arguments.amount} message(s)."
            }
        }
    }
}

private class CleanArgs : Arguments() {
    val amount by defaultingInt {
        name = "amount"
        description = "The amount of messages to clean."
        defaultValue = 1
    }
    val reason by optionalString {
        name = "reason"
        description = "Reason for deletion."
    }
}