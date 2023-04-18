package bogus.lib.database

import com.kotlindiscord.kord.extensions.extensions.Extension
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.koin.core.component.inject
import javax.sql.DataSource

/**
 * Migrate a database with the specified resource path.
 * @param path resource path
 */
fun Extension.migrate(path: String, schema: String) {
    val hikari by inject<DataSource>()
    val log = KotlinLogging.logger { }

    log.info { "Migrating — path=$path" }

    Flyway.configure()
        .table("${schema}_schema_history")
        .dataSource(hikari)
        .locations(path)
        .baselineOnMigrate(true)
        .load()
        .migrate()
}