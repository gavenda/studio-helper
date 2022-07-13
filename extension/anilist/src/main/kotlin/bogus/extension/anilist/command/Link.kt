package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.check.anyGuild
import bogus.extension.anilist.db.DbUser
import bogus.extension.anilist.db.users
import bogus.extension.anilist.graphql.AniList
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

suspend fun AniListExtension.link() {
    val db by inject<Database>()
    val aniList by inject<AniList>()

    ephemeralSlashCommand(::LinkArgs) {
        name = "command.link"
        description = "command.link.description"
        check {
            anyGuild(translate("link.error.server-only"))
        }
        action {
            val guild = guild ?: return@action

            val existingUser = db.users.firstOrNull {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            if (existingUser != null) {
                respond {
                    content = translate("link.error.already-linked")
                }
                return@action
            }

            val aniListUser = aniList.findUserByName(arguments.username)

            if (aniListUser == null) {
                respond {
                    content = translate("link.successful")
                }
                return@action
            }

            db.users.add(
                DbUser {
                    aniListId = aniListUser.id
                    aniListUsername = aniListUser.name
                    discordId = user.idLong
                    discordGuildId = guild.idLong
                }
            )

            respond {
                content = translate("link.successful")
            }
        }
    }

    ephemeralSlashCommand {
        name = "command.unlink"
        description = "command.unlink.description"
        check {
            anyGuild(translate("unlink.error.server-only"))
        }
        action {
            val guild = guild ?: return@action

            val existingUser = db.users.firstOrNull {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            if (existingUser == null) {
                respond {
                    content = translate("unlink.error.not-linked")
                }
                return@action
            }

            log.info {
                message = "Unlinking AniList from Discord"
                context = mapOf(
                    "aniListUsername" to existingUser.aniListUsername,
                    "discordUserId" to user.id,
                    "guildId" to guild.id
                )
            }

            db.users.removeIf {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            respond {
                content = translate("unlink.successful")
            }
        }
    }
}

private class LinkArgs : Arguments() {
    val username by coalescingString {
        name = "command.link.args.username"
        description = "command.link.args.username.description"
    }
}