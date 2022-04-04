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
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.interaction.suggestInt
import org.koin.core.component.KoinComponent
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
                    setupPolling(guild)

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
                val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq guild.idLong } ?: DbGuild {
                    discordGuildId = guild.idLong
                    hentai = false
                    locale = Locale.getDefault().toLanguageTag()
                }

                dbGuild.notificationChannelId = notificationChannel.id.value.toLong()
                dbGuild.flushChanges()

                respond {
                    content = translate("notification.bind.response", notificationChannel.mention)
                }
            }
        }
    }
}

internal class AiringAnimeArgs : KoinComponent, Arguments() {
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

internal class BindNotificationArgs : KoinComponent, Arguments() {
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