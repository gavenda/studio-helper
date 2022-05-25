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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
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
import java.util.concurrent.Executors
import javax.sql.DataSource
import kotlin.time.Duration.Companion.hours

object AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    val log = KotlinLogging.logger { }.asLogFMT()

    private val pollingQueueDispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        .asCoroutineDispatcher()
    private val notificationPollDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
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
            }
        }
        event<GuildCreateEvent> {
            action {
                setupPolling(event.guild)
            }
        }
    }

    fun removeAnimeFromPolling(guildId: Snowflake, mediaId: Long) {
        val poller = pollers[guildId] ?: return

        log.info(
            msg = "Remove airing anime from polling",
            context = mapOf(
                "guild" to guildId,
                "mediaId" to mediaId
            )
        )

        if (poller.mediaIds.isEmpty()) {
            poller.close()
            pollers.remove(guildId)
        }

        poller.removeMediaId(mediaId)
    }

    suspend fun setupPolling(guild: GuildBehavior) {
        val db by inject<Database>()
        val mediaIds = db.airingAnimes
            .filter { it.discordGuildId eq guild.idLong }
            .map { it.mediaId }

        if (mediaIds.isNotEmpty()) {
            log.info(
                msg = "Begin airing anime polling",
                context = mapOf(
                    "guildId" to guild.id,
                    "mediaIds" to mediaIds
                )
            )

            val runningPoller = pollers[guild.id]

            if (runningPoller != null) {
                runningPoller.setupMediaIds(mediaIds)
                return
            }

            val poller = AiringSchedulePoller(pollingQueueDispatcher, mediaIds)
            pollers.computeIfAbsent(guild.id) { poller }

            CoroutineScope(notificationPollDispatcher).launch {
                poller.poll(1.hours).collect { airingSchedules ->
                    val titles = airingSchedules
                        .sortedByDescending { it.episode }
                        .distinctBy { it.mediaId }
                        .mapNotNull { it.media.title?.english ?: it.media.title?.romaji }

                    log.info(
                        msg = "Collecting airing schedules",
                        context = mapOf(
                            "guildId" to guild.id,
                            "titles" to titles
                        )
                    )

                    val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }
                    airingSchedules.forEach { airingSchedule ->
                        val channel = guild.getChannelOf<GuildMessageChannel>(Snowflake(dbGuild.notificationChannelId))
                        val mediaTitle = airingSchedule.media.title?.english ?: airingSchedule.media.title?.romaji

                        if (!channel.botHasPermissions(Permission.SendMessages, Permission.ViewChannel)) {
                            return@forEach
                        }

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