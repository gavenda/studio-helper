package bogus.extension.anilist.command

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.db.*
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.MediaType
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.suggestInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.firstOrNull
import java.util.*

suspend fun AniListExtension.notification() {
    ephemeralSlashCommand {
        name = "command.notification"
        description = "command.notification.description"
        allowInDms = false
        group("command.notification.airing-anime") {
            description = "command.notification.airing-anime.description"
            ephemeralSubCommand(::AiringAnimeArgs) {
                name = "command.notification.airing-anime.add"
                description = "command.notification.airing-anime.add.description"
                check {
                    anyGuild()
                }
                action {
                    val guild = guild ?: return@action
                    val dbAiringAnime = db.airingAnimes.firstOrNull {
                        (it.discordGuildId eq guild.idLong) and (it.mediaId eq arguments.mediaId)
                    }

                    if (dbAiringAnime != null) {
                        respond {
                            content = translate("notification.airing-anime.add.fail")
                        }
                        return@action
                    }

                    val newDbAiringAnime = DbAiringAnime {
                        discordGuildId = guild.idLong
                        mediaId = arguments.mediaId
                        userId = user.idLong
                    }

                    db.airingAnimes.add(newDbAiringAnime)

                    CoroutineScope(Dispatchers.IO).launch {
                        notifier.schedule(guild)
                    }

                    respond {
                        content = translate("notification.airing-anime.add.success")
                    }
                }
            }

            ephemeralSubCommand(::AiringAnimeArgs) {
                name = "command.notification.airing-anime.remove"
                description = "command.notification.airing-anime.remove.description"
                check {
                    anyGuild()
                }
                action {
                    val guild = guild ?: return@action
                    val dbAiringAnime = db.airingAnimes.firstOrNull {
                        (it.discordGuildId eq guild.idLong) and (it.mediaId eq arguments.mediaId)
                    }

                    if (dbAiringAnime == null) {
                        respond {
                            content = translate("notification.airing-anime.remove.fail")
                        }
                        return@action
                    }

                    dbAiringAnime.delete()
                    notifier.remove(guild.id, arguments.mediaId)

                    respond {
                        content = translate("notification.airing-anime.remove.success")
                    }
                }
            }
        }

        ephemeralSubCommand(::BindNotificationArgs) {
            name = "command.notification.bind"
            description = "command.notification.bind.description"
            check {
                anyGuild()
            }
            action {
                val guild = guild ?: return@action
                val notificationChannel = arguments.channel ?: channel
                val notificationChannelIdLong = notificationChannel.id.value.toLong()
                val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guild.idLong }
                val guildChannel = guild.getChannel(notificationChannel.id)

                if (!guildChannel.botHasPermissions(Permission.SendMessages, Permission.ViewChannel)) {
                    respond {
                        content = translate("notification.bind.response.no-permission")
                    }
                    return@action
                }

                if (dbGuild != null) {
                    dbGuild.notificationChannelId = notificationChannelIdLong
                    dbGuild.flushChanges()
                } else {
                    val newDbGuild = DbGuild {
                        discordGuildId = guild.idLong
                        hentai = false
                        locale = Locale.getDefault().toLanguageTag()
                        notificationChannelId = notificationChannelIdLong
                    }
                    db.guilds.add(newDbGuild)
                }

                respond {
                    content = translate("notification.bind.response", notificationChannel.mention)
                }
            }
        }

        ephemeralSubCommand() {
            name = "command.notification.clear"
            description = "command.notification.clear.description"
            check {
                anyGuild()
                requirePermission(Permission.Administrator)
            }
            action {
                val guild = guild ?: return@action

                db.delete(DbAiringAnimes) { it.discordGuildId eq guild.idLong }

                respond {
                    content = translate("notification.clear.response")
                }
            }
        }
    }
}

private class AiringAnimeArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val mediaId by long {
        name = "command.notification.airing-anime.add.args.media-id"
        description = "command.notification.airing-anime.add.args.media-id.description"
        autoComplete {
            suggestInteger {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestInteger

                aniList.findMediaTitles(input, MediaType.ANIME)
                    ?.take(AUTOCOMPLETE_ITEMS_LIMIT)
                    ?.forEach { media ->
                        val mediaTitle = media.title?.english ?: media.title?.romaji
                        if (mediaTitle != null) {
                            choice(mediaTitle, media.id)
                        }
                    }
            }
        }
    }
}

private class BindNotificationArgs : KordExKoinComponent, Arguments() {
    val channel by optionalChannel {
        name = "command.notification.bind.args.channel"
        description = "command.notification.bind.args.channel.description"

        requireChannelType(ChannelType.GuildText)
        requireChannelType(ChannelType.PublicGuildThread)
        requireChannelType(ChannelType.PrivateThread)
    }
}