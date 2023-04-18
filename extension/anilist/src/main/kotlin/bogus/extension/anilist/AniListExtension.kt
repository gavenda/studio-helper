package bogus.extension.anilist

import bogus.extension.anilist.command.*
import bogus.extension.anilist.command.message.characterMessageCommand
import bogus.extension.anilist.command.message.findMessageCommand
import bogus.extension.anilist.command.message.staffMessageCommand
import bogus.extension.anilist.command.message.userMessageCommand
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.graphql.AniListGraphQL
import bogus.lib.database.migrate

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import java.util.concurrent.Executors

class AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    val db by inject<Database>()
    val aniList by inject<AniList>()
    val notifier by inject<NotifyScheduler>()
    val log = KotlinLogging.logger { }
    val notifierContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    companion object {
        var EMBED_COLOR = 0
    }

    override suspend fun setup() {
        setupDatabase()
        setupNotifier()
        setupGraphQL()
        setupEvents()
        setupCommands()
        setupMessageCommands()
    }

    private suspend fun setupEvents() {
        event<ReadyEvent> {
            action {
                CoroutineScope(notifierContext).launch {
                    notifier.start()
                }
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
        try {
            migrate(
                path = "classpath:db/anilist/migration",
                schema = EXTENSION_NAME
            )
        } catch (ex: Exception) {
            log.error(ex) { "Error setting up database" }
        }
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