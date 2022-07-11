package bogus.lib.database

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import javax.sql.DataSource

/**
 * Sets up a database pool for this bot.
 */
suspend fun ExtensibleBot.setupDatabase() {
    val log = KotlinLogging.logger { }

    loadModule {
        single<DataSource>(createdAtStart = true) {
            log.info { "Initializing data source" }
            HikariDataSource(HikariConfig().apply {
                maximumPoolSize = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                jdbcUrl = env("DB_URL")
                username = env("DB_USER")
                password = env("DB_PASS")
            })
        }
        single {
            Database.connect(
                dataSource = get(),
                dialect = PostgreSqlDialect()
            )
        }
    }
}
