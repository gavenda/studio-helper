package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.check.anyGuild
import bogus.extension.anilist.db.DbUser
import bogus.extension.anilist.db.users
import bogus.extension.anilist.graphql.AniList
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
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
        name = "link"
        description = "Link your Discord account to your AniList account."
        check {
            anyGuild(translate("link.error.serverOnly"))
        }
        action {
            val guild = guild ?: return@action

            val existingUser = db.users.firstOrNull {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            if (existingUser != null) {
                respond {
                    content = translate("link.error.alreadyLinked")
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
        name = "unlink"
        description = "Unlink your AniList account from your Discord account."
        check {
            anyGuild(translate("unlink.error.serverOnly"))
        }
        action {
            val guild = guild ?: return@action

            val existingUser = db.users.firstOrNull {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            if (existingUser == null) {
                respond {
                    content = translate("unlink.error.notLinked")
                }
                return@action
            }

            log.debug { "Unlinking AniList user [ ${existingUser.aniListUsername} ] from Discord [ user = ${user.idLong}, guild = ${guild.idLong} ]" }

            db.users.removeIf {
                (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
            }

            respond {
                content = translate("unlink.successful")
            }
        }
    }
}

internal class LinkArgs : Arguments() {
    val username by string {
        name = "username"
        description = "AniList username, defaults to your own if linked."
    }
}