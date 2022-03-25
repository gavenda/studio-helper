package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.users
import bogus.extension.anilist.embed.createUserEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.util.LRUCache
import bogus.util.abbreviate
import bogus.util.action
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

suspend fun AniListExtension.user() {
    val aniList by inject<AniList>()
    val db by inject<Database>()

    publicSlashCommand(::UserArgs) {
        name = "user"
        description = "Looks up the statistics of a user's AniList."
        action(Dispatchers.IO) {
            val username = arguments.username

            if (username != null) {
                val user = aniList.findUserStatisticsByName(username)
                if (user == null) {
                    respond {
                        content = translate("user.error.userNotFound")
                    }
                } else if (user.statistics == null) {
                    respond {
                        content = translate("user.error.noUserStatistics")
                    }
                } else {
                    respond {
                        embed {
                            apply(createUserEmbed(user))
                        }
                        components {
                            linkButton {
                                label = "Follow on AniList"
                                url = user.siteUrl
                            }
                        }
                    }
                }
            } else {
                if (guild == null) {
                    respond {
                        content = translate("user.error.usernameRequired")
                    }
                    return@action
                }
                val guild = guild ?: return@action

                val dbUsername = db.users.firstOrNull {
                    (it.discordId eq user.idLong) and (it.discordGuildId eq guild.idLong)
                }?.aniListUsername

                if (dbUsername == null) {
                    respond {
                        content = translate("user.error.accountNotLinked")
                    }
                    return@action
                }

                val user = aniList.findUserStatisticsByName(dbUsername)

                // Linked, but not found
                if (user == null) {
                    respond {
                        content = translate("user.error.linkNotFound")
                    }
                } else if (user.statistics == null) {
                    respond {
                        content = translate("user.error.noUserStatistics")
                    }
                } else {
                    respond {
                        embed {
                            apply(createUserEmbed(user))
                        }
                        components {
                            linkButton {
                                label = "Follow on AniList"
                                url = user.siteUrl
                            }
                        }
                    }
                }
            }
        }
    }

    publicUserCommand {
        name = "Show AniList"
        check {
            anyGuild()
        }
        action(Dispatchers.IO) {
            val guild = guild ?: return@action
            val targetUser = targetUsers.first()

            val dbUsername = db.users.firstOrNull {
                (it.discordId eq targetUser.idLong) and (it.discordGuildId eq guild.idLong)
            }?.aniListUsername

            if (dbUsername == null) {
                respond {
                    content = translate("user.error.userNotLinked")
                }
                return@action
            }

            val user = aniList.findUserStatisticsByName(dbUsername)

            // Linked, but not found
            if (user == null) {
                respond {
                    content = translate("user.error.linkNotFound")
                }
            } else if (user.statistics == null) {
                respond {
                    content = translate("user.error.noUserStatistics")
                }
            } else {
                respond {
                    embed {
                        apply(createUserEmbed(user))
                    }
                    components {
                        linkButton {
                            label = "Follow on AniList"
                            url = user.siteUrl
                        }
                    }
                }
            }
        }
    }
}

internal class UserArgs : KoinComponent, Arguments() {
    companion object {
        val cache = LRUCache<String, List<String>>(50)
    }

    val aniList by inject<AniList>()
    val username by optionalString {
        name = "username"
        description = "AniList username, defaults to your own if linked."

        autoComplete {
            suggestString {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestString
                val cacheLookup = cache[input]

                if (cacheLookup != null) {
                    cacheLookup.forEach { choice(it, it) }
                } else {
                    aniList.findUserNames(input)
                        .apply { cache[input] = this }
                        .map { it.abbreviate(80) }
                        .forEach { choice(it, it) }
                }
            }
        }
    }
}
