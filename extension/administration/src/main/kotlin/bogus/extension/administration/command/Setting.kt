package bogus.extension.administration.command

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.db.DbGuild
import bogus.extension.administration.db.guilds
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingOptionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
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
        name = "command.setting"
        description = "command.setting.description"
        requirePermission(Permission.Administrator)
        group("command.setting.messages") {
            description = "command.setting.messages.description"
            welcome()
            leave()
        }
    }
}

private suspend fun SlashGroup.welcome() {
    val db by inject<Database>()

    ephemeralSubCommand(::MessageArguments) {
        name = "command.setting.messages.welcome"
        description = "command.setting.messages.welcome.description"
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
        name = "command.setting.messages.leave"
        description = "command.setting.messages.leave.description"
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

private class MessageArguments : Arguments() {
    val textChannelId by optionalChannel {
        name = "command.setting.messages.args.text-channel"
        description = "command.setting.messages.args.text-channel.description"
    }
    val message by coalescingOptionalString {
        name = "command.setting.messages.args.message"
        description = "command.setting.messages.args.message.description"
    }
}