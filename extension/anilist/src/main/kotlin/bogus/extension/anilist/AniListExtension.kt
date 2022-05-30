package bogus.extension.anilist

import bogus.extension.anilist.command.*
import bogus.extension.anilist.coroutines.AiringSchedulePoller
import bogus.extension.anilist.db.airingAnimes
import bogus.extension.anilist.db.guilds
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.graphql.AniListGraphQL
import bogus.util.asLogFMT
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.first
import org.ktorm.entity.map
import org.ktorm.support.postgresql.PostgreSqlDialect
import javax.sql.DataSource

object AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    private val log = KotlinLogging.logger { }.asLogFMT()
    private val kronScheduler = buildSchedule {
        minutes {
            from(0) every 30
        }
    }
    private val pollers = mutableMapOf<Snowflake, AiringSchedulePoller>()

    override suspend fun setup() {
        setupEvents()
        setupCommands()
    }

    private suspend fun setupEvents() {
        event<ReadyEvent> {
            action {
                setupDatabase()
                setupGraphQL()
                startPoll()
            }
        }
        event<GuildCreateEvent> {
            action {
                beginPoll(event.guild)
            }
        }
    }

    suspend fun startPoll() = kronScheduler.doInfinity {
        val db by inject<Database>()
        pollers.forEach { (guildId, poller) ->
            val guild = kord.getGuild(guildId) ?: return@forEach
            val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }
            val airingSchedules = poller.poll()

            airingSchedules.forEach { airingSchedule ->
                val channel = guild.getChannelOf<GuildMessageChannel>(Snowflake(dbGuild.notificationChannelId))
                val mediaTitle = airingSchedule.media.title?.english ?: airingSchedule.media.title?.romaji

                if (channel.botHasPermissions(Permission.SendMessages, Permission.ViewChannel)) {
                    channel.createEmbed {
                        title = "New Episode Aired!"
                        description = "Episode **${airingSchedule.episode}** of **$mediaTitle** has aired."
                        color = Color(0xFF0000)
                        thumbnail {
                            url = airingSchedule.media.coverImage?.extraLarge ?: ""
                        }
                    }
                }
            }
        }
    }

    fun removeAnimeFromPolling(guildId: Snowflake, mediaId: Long) {
        val poller = pollers[guildId] ?: return

        if (poller.mediaIds.isEmpty()) {
            pollers.remove(guildId)
        }

        poller.removeMediaId(mediaId)
    }

    fun beginPoll(guild: GuildBehavior) {
        val db by inject<Database>()
        val mediaIds = db.airingAnimes
            .filter { it.discordGuildId eq guild.idLong }
            .map { it.mediaId }

        if (mediaIds.isNotEmpty()) {
            val poller = pollers[guild.id]

            if (poller != null) {
                poller.updateMediaIds(mediaIds)
                return
            }

            pollers.computeIfAbsent(guild.id) { AiringSchedulePoller(mediaIds) }
        } else {
            log.warn(
                msg = "No media id given, will not poll",
                context = mapOf(
                    "guildId" to guild.id
                )
            )
        }
    }

    private fun setupGraphQL() {
        loadModule {
            single {
                HttpClient(CIO)
            }
            single<AniList> {
                AniListGraphQL()
            }
            single {
                Json {
                    encodeDefaults = false
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                }
            }
        }
    }

    private fun setupDatabase() {
        loadModule {
            single<DataSource>(createdAtStart = true) {
                HikariDataSource(HikariConfig().apply {
                    maximumPoolSize = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                    jdbcUrl = env("ANILIST_DB_URL")
                    username = env("ANILIST_DB_USER")
                    password = env("ANILIST_DB_PASS")
                })
            }
            single {
                Database.connect(
                    dataSource = get(),
                    dialect = PostgreSqlDialect()
                )
            }
        }

        val hikari by inject<DataSource>()

        Flyway.configure()
            .dataSource(hikari)
            .locations("db.anilist.migration")
            .load()
            .migrate()
    }

    private suspend fun setupCommands() {
        character()
        find()
        link()
        notification()
        ranking()
        staff()
        user()
    }
}