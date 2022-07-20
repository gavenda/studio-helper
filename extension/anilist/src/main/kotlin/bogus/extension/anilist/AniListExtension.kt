package bogus.extension.anilist

import bogus.extension.anilist.command.*
import bogus.extension.anilist.command.message.characterMessageCommand
import bogus.extension.anilist.command.message.findMessageCommand
import bogus.extension.anilist.command.message.staffMessageCommand
import bogus.extension.anilist.command.message.userMessageCommand
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.graphql.AniListGraphQL
import bogus.lib.database.migrate
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database

class AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    val db by inject<Database>()
    val aniList by inject<AniList>()
    val notifier by inject<NotifyScheduler>()
    val log = KotlinLogging.logger { }.asFMTLogger()

    companion object {
        var EMBED_COLOR = 0
    }

    override suspend fun setup() {
        setupEvents()
        setupCommands()
        setupMessageCommands()
        setupDatabase()
        setupNotifier()
        setupGraphQL()
    }

    private suspend fun setupEvents() {
        event<ReadyEvent> {
            action {
                notifier.start()
            }
        }
        event<GuildCreateEvent> {
            action {
                notifier.schedule(event.guild)
            }
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

    private fun setupNotifier() {
        loadModule {
            single<NotifyScheduler> {
                NotifyScheduler(kord)
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