package bogus.extension.counter

import bogus.extension.counter.command.count
import bogus.lib.database.migrate
import com.kotlindiscord.kord.extensions.extensions.Extension

class CounterExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME

    companion object {
        var EMBED_COLOR = 0
    }

    override suspend fun setup() {
        setupDatabase()
        setupCommands()
    }

    private suspend fun setupCommands() {
        count()
    }

    private fun setupDatabase() {
        migrate(
            path = "classpath:db/counter/migration"
        )
    }
}
