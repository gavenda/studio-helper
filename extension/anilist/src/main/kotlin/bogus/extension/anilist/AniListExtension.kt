package bogus.extension.anilist

import bogus.extension.anilist.command.*
import bogus.extension.anilist.command.message.characterMessageCommand
import bogus.extension.anilist.command.message.findMessageCommand
import bogus.extension.anilist.command.message.staffMessageCommand
import bogus.extension.anilist.command.message.userMessageCommand
import bogus.extension.anilist.coroutines.AiringSchedulePoller
import bogus.extension.anilist.db.airingAnimes
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.graphql.AniListGraphQL
import bogus.lib.database.migrate
import bogus.util.asFMTLogger
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.supplier.EntitySupplyStrategy
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.map

object AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    val db by inject<Database>()
    val log = KotlinLogging.logger { }.asFMTLogger()

    var EMBED_COLOR = 0

    private val kronScheduler = buildSchedule {
        minutes {
            from(0) every 30
        }
    }
    private val pollers = mutableMapOf<Snowflake, AiringSchedulePoller>()

    override suspend fun setup() {
        setupEvents()
        setupCommands()
        setupMessageCommands()
        setupDatabase()
        setupGraphQL()
    }

    private suspend fun setupEvents() {
        event<ReadyEvent> {
            action {
                scheduleNotify()
            }
        }
        event<GuildCreateEvent> {
            action {
                scheduleNotify(event.guild)
            }
        }
    }

    private suspend fun scheduleNotify() = kronScheduler.doInfinity {
        pollers.forEach { (guildId, poller) ->
            val guild = kord.getGuild(guildId, EntitySupplyStrategy.cache) ?: return@forEach
            notify(guild, poller)
        }
    }

    fun removeAnimeSchedule(guildId: Snowflake, mediaId: Long) {
        val poller = pollers[guildId] ?: return

        if (poller.mediaIds.isEmpty()) {
            pollers.remove(guildId)
        }

        poller.removeMediaId(mediaId)
    }

    fun scheduleNotify(guild: GuildBehavior) {
        val mediaIds = db.airingAnimes
            .filter { it.discordGuildId eq guild.idLong }
            .map { it.mediaId }
        if (mediaIds.isNotEmpty()) {
            pollers.computeIfAbsent(guild.id) { AiringSchedulePoller(mediaIds) }
                .updateMediaIds(mediaIds)
            return
        }

        log.warn {
            message = "No media id given, will not poll"
            context = mapOf(
                "guildId" to guild.id
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
        migrate(
            path = "classpath:db/anilist/migration",
            schema = EXTENSION_NAME
        )
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

    private suspend fun setupMessageCommands() {
        characterMessageCommand()
        findMessageCommand()
        staffMessageCommand()
        userMessageCommand()
    }
}