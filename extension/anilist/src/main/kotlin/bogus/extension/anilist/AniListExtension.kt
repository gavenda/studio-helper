package bogus.extension.anilist

import bogus.extension.anilist.command.*
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.graphql.AniListGraphQL
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.kord.core.event.gateway.ReadyEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import javax.sql.DataSource

object AniListExtension : Extension() {
    override val name = "anilist"
    override val bundle = "anilist"

    val log = KotlinLogging.logger { }

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
                    maximumPoolSize = Runtime.getRuntime().availableProcessors() / 2
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
            .load()
            .migrate()
    }

    private suspend fun setupCommands() {
        character()
        find()
        link()
        ranking()
        staff()
        user()
    }
}