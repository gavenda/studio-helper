package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.db.DbGuild
import bogus.extension.administration.db.guilds
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull

suspend fun AdministrationExtension.setting() {
    ephemeralSlashCommand {
        name = "setting"
        description = "setting.description"
        requirePermission(Permission.Administrator)
        group("messages") {
            description = "setting.messages.description"
            welcome()
            leave()
        }
    }
}

private suspend fun SlashGroup.welcome() {
    val db by inject<Database>()

    ephemeralSubCommand(::MessageArguments) {
        name = "welcome"
        description = "setting.messages.welcome.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guild.idLong } ?: DbGuild {
                discordGuildId = guild.idLong
            }.apply {
                db.guilds.add(this)
            }

            if (arguments.message != null) {
                TODO("Not yet supported")
            }

            if (arguments.textChannelId != null) {
                dbGuild.welcomeChannelId = arguments.textChannelId?.idLong
                dbGuild.flushChanges()
            }

            respond {
                content = translate("setting.messages.respond.welcome")
            }
        }
    }
}

private suspend fun SlashGroup.leave() {
    val db by inject<Database>()

    ephemeralSubCommand(::MessageArguments) {
        name = "leave"
        description = "setting.messages.leave.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guild.idLong } ?: DbGuild {
                discordGuildId = guild.idLong
            }.apply {
                db.guilds.add(this)
            }

            if (arguments.message != null) {
                TODO("Not yet supported")
            }

            if (arguments.textChannelId != null) {
                dbGuild.leaveChannelId = arguments.textChannelId?.idLong
                dbGuild.flushChanges()
            }

            respond {
                content = translate("setting.messages.respond.leave")
            }
        }
    }
}

internal class MessageArguments : Arguments() {
    val textChannelId by optionalChannel {
        name = "text-channel-id"
        description = "setting.messages.arguments.text-channel-id"
    }
    val message by optionalString {
        name = "message"
        description = "setting.messages.arguments.message"
    }
}