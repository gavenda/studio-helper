package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.users
import bogus.extension.anilist.embed.createUserEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.util.abbreviate
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun AniListExtension.user() {
    val aniList by inject<AniList>()
    val db by inject<Database>()

    publicSlashCommand(::UserArgs) {
        name = "command.user"
        description = "command.user.description"
        action {
            val username = arguments.username

            if (username != null) {
                val user = aniList.findUserStatisticsByName(username)
                if (user == null) {
                    respond {
                        content = translate("user.error.user-not-found")
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
            } else {
                if (guild == null) {
                    respond {
                        content = translate("user.error.username-required")
                    }
                    return@action
                }
                val guild = guild ?: return@action

                val dbUsername = db.users.firstOrNull {
                    (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
                }?.aniListUsername

                if (dbUsername == null) {
                    respond {
                        content = translate("user.error.account-not-linked")
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
}

private class UserArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val username by optionalString {
        name = "command.user.args.username"
        description = "command.user.args.username.description"

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString

                aniList.findUserNames(input)
                    .map { it.abbreviate(80) }
                    .forEach { choice(it, it) }
            }
        }
    }
}
