package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.TRANSLATIONS_BUNDLE
import bogus.extension.anilist.db.DbAiringAnime
import bogus.extension.anilist.db.DbGuild
import bogus.extension.anilist.db.airingAnimes
import bogus.extension.anilist.db.guilds
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
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.suggestInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.firstOrNull
import java.util.*

suspend fun AniListExtension.notification() {
    val db by inject<Database>()

    ephemeralSlashCommand {
        name = "notification"
        description = "Setup notifications."

        group("airing-anime") {
            description = "Airing anime notifications."
            ephemeralSubCommand(::AiringAnimeArgs) {
                name = "add"
                description = "notification.airingAnime.add.description"
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
                            content = translate("notification.airingAnime.add.fail")
                        }
                        return@action
                    }

                    val newDbAiringAnime = DbAiringAnime {
                        discordGuildId = guild.idLong
                        mediaId = arguments.mediaId
                    }

                    db.airingAnimes.add(newDbAiringAnime)

                    CoroutineScope(Dispatchers.IO).launch {
                        beginPoll(guild)
                    }

                    respond {
                        content = translate("notification.airingAnime.add.success")
                    }
                }
            }

            ephemeralSubCommand(::AiringAnimeArgs) {
                name = "remove"
                description = "notification.airingAnime.remove.description"
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
                            content = translate("notification.airingAnime.remove.fail")
                        }
                        return@action
                    }

                    dbAiringAnime.delete()
                    removeAnimeFromPolling(guild.id, arguments.mediaId)

                    respond {
                        content = translate("notification.airingAnime.remove.success")
                    }
                }
            }
        }

        ephemeralSubCommand(::BindNotificationArgs) {
            name = "bind"
            description = "notification.bind.description"
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
                        content = translate("notification.bind.response.noPermission")
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
    }
}

private class AiringAnimeArgs : KordExKoinComponent, Arguments() {
    val aniList by inject<AniList>()
    val tp by inject<TranslationsProvider>()
    val mediaId by long {
        name = "media-id"
        description = tp.translate(
            key = "notification.airingAnime.args.anime",
            bundleName = TRANSLATIONS_BUNDLE
        )
        autoComplete {
            suggestInt {
                val input = focusedOption.value
                if (input.isBlank()) return@suggestInt

                aniList.findMediaTitles(input, MediaType.ANIME)?.forEach { media ->
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
    val tp by inject<TranslationsProvider>()
    val channel by optionalChannel {
        name = "channel"
        description = tp.translate(
            key = "notification.bind.args.channel",
            bundleName = TRANSLATIONS_BUNDLE
        )

        requireChannelType(ChannelType.GuildText)
    }
}