package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.db.DbGuild
import bogus.extension.music.db.DbGuilds
import bogus.extension.music.db.guilds
import bogus.extension.music.player
import bogus.util.action
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull

suspend fun MusicExtension.unbind() {
    val db by inject<Database>()

    ephemeralSlashCommand {
        name = "unbind"
        description = "unbind.description"
        check {
            anyGuild()
            hasPermission(Permission.Administrator)
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action

            if (player.bound) {
                val dbGuild = db.guilds.firstOrNull { DbGuilds.discordGuildId eq guild.idLong } ?: DbGuild {
                    discordGuildId = guild.idLong
                }.also { db.guilds.add(it) }
                val textChannel = dbGuild.textChannelId?.let { getGuild()?.asGuild()?.getChannel(Snowflake(it)) }

                dbGuild.textChannelId = null
                dbGuild.lastMessageId = null
                dbGuild.flushChanges()
                player.unbind()

                log.info { """msg="Guild unbound" guild=${guild.idLong}""" }

                if (textChannel != null) {
                    respond {
                        content = translate("unbind.response.success", arrayOf(channel.mention))
                    }
                } else {
                    respond {
                        content = translate("unbind.response.semi")
                    }
                }
            } else {
                respond {
                    content = translate("unbind.response.fail")
                }
            }
        }
    }
}

suspend fun MusicExtension.bind() {
    val db by inject<Database>()

    ephemeralSlashCommand {
        name = "bind"
        description = "bind.description"
        check {
            anyGuild()
            hasPermission(Permission.Administrator)
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val dbGuild = db.guilds.firstOrNull { DbGuilds.discordGuildId eq guild.idLong } ?: DbGuild {
                discordGuildId = guild.idLong
            }.also { db.guilds.add(it) }

            dbGuild.textChannelId = channel.id.value.toLong()
            dbGuild.lastMessageId = player.bind(channel)?.value?.toLong() ?: 0L
            dbGuild.flushChanges()

            log.info { """msg="Guild bound" guild=${guild.idLong} channel=${channel.id}""" }

            respond {
                content = translate("bind.response", arrayOf(channel.mention))
            }
        }
    }
}