package bogus.extension.anilist.command.message

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.users
import bogus.extension.anilist.embed.createUserEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun AniListExtension.userMessageCommand() {
    val aniList by inject<AniList>()
    val db by inject<Database>()
    publicUserCommand {
        name = "command.user.message-command"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val targetUser = targetUsers.first()

            val dbUsername = db.users.firstOrNull {
                (it.discordId eq targetUser.idLong) and (it.discordGuildId eq guild.idLong)
            }?.aniListUsername

            if (dbUsername == null) {
                respond {
                    content = translate("user.error.user-not-linked")
                }
                return@action
            }

            val user = aniList.findUserStatisticsByName(dbUsername)

            // Linked, but not found
            if (user == null) {
                respond {
                    content = translate("user.error.link-not-found")
                }
            } else if (user.statistics == null) {
                respond {
                    content = translate("user.error.no-user-statistics")
                }
            } else {
                respond {
                    embed {
                        apply(createUserEmbed(user))
                    }
                    components {
                        linkButton {
                            label = translate("user.link.label")
                            url = user.siteUrl
                        }
                    }
                }
            }
        }
    }
}