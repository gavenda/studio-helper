package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.embed.createEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.util.abbreviate
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject

suspend fun AniListExtension.user() {
    publicSlashCommand(::UserArgs) {
        name = "command.user"
        description = "command.user.description"
        action {
            val username = arguments.username
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

private class UserArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val username by string {
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
