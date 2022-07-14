package bogus.extension.moderation

import bogus.extension.moderation.command.clean
import bogus.lib.database.migrate
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
class ModerationExtension : Extension() {
    override val name = EXTENSION_NAME
    override suspend fun setup() {
        configureIntents()
        setupDatabase()
        setupCommands()
    }

    private fun configureIntents() {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }

    private suspend fun setupCommands() {
        clean()
    }

    private suspend fun setupMessageCommands() {

    }

    private fun setupDatabase() {
        migrate(
            path = "classpath:db/moderation/migration",
            schema = EXTENSION_NAME
        )
    }

}