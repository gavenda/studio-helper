package bogus.extension.anilist.command.message

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.users
import bogus.extension.anilist.embed.createEmbed
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun AniListExtension.userMessageCommand() {
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
                        apply(user.createEmbed())
                    }
                    components {
                        linkButton {
                            label = translate("user.link.profile")
                            url = user.siteUrl
                        }
                        linkButton {
                            label = translate("user.link.anime-list")
                            url = "${user.siteUrl}/animelist"
                        }
                        linkButton {
                            label = translate("user.link.manga-list")
                            url = "${user.siteUrl}/mangalist"
                        }
                        linkButton {
                            label = translate("user.link.stats")
                            url = "${user.siteUrl}/stats/anime/overview"
                        }
                    }
                }
            }
        }
    }
}