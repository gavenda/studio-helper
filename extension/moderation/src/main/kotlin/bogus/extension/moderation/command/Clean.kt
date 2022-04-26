package bogus.extension.moderation.command

import bogus.extension.moderation.ModerationExtension
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

suspend fun ModerationExtension.clean() {
    val log = KotlinLogging.logger {  }
        .asLogFMT()

    ephemeralSlashCommand(::CleanArgs) {
        name = "clean"
        description = "Cleans a message up to 100 messages."
        check {
            anyGuild()
        }
        requireBotPermissions(Permission.ManageMessages)
        action {
            val gmc = channel.asChannelOf<GuildMessageChannel>()
            val limit = arguments.amount + 1
            val lastMessage = gmc.lastMessage ?: return@action

            log.info(
                msg = "Cleaning messages",
                context = mapOf(
                    "amount" to arguments.amount,
                    "lastMessageId" to lastMessage.id,
                    "reason" to arguments.reason
                )
            )

            val messages = gmc.getMessagesBefore(lastMessage.id, limit)
                .map { it.id }
                .toList()

            gmc.bulkDelete(messages, arguments.reason)

            respond {
                content = "Cleaned ${arguments.amount} message(s)."
            }
        }
    }
}

internal class CleanArgs : Arguments() {
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