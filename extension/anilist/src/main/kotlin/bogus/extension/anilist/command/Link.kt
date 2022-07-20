package bogus.extension.anilist.command

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.DbUser
import bogus.extension.anilist.db.users
import bogus.extension.anilist.graphql.AniList
import bogus.util.abbreviate
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import org.koin.core.component.inject
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import org.ktorm.entity.removeIf

suspend fun AniListExtension.link() {
    ephemeralSlashCommand(::LinkArgs) {
        name = "command.link"
        description = "command.link.description"
        allowInDms = false
        check {
            anyGuild()
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
        allowInDms = false
        check {
            anyGuild()
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

private class LinkArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val username by coalescingString {
        name = "command.link.args.username"
        description = "command.link.args.username.description"

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                aniList.findUserNames(input)
                    .take(AUTOCOMPLETE_ITEMS_LIMIT)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}