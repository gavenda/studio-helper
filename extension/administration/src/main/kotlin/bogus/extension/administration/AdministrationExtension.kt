package bogus.extension.administration

import bogus.extension.administration.command.broadcast
import bogus.extension.administration.command.leaveMessage
import bogus.extension.administration.command.setting
import bogus.extension.administration.command.welcomeMessage
import bogus.extension.administration.event.leaveMessageEvent
import bogus.extension.administration.event.welcomeMessageEvent
import bogus.lib.database.migrate
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
class AdministrationExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME

    override suspend fun setup() {
        configureIntents()
        setupDatabase()
        setupEvents()
        setupSlashCommands()
    }

    private fun configureIntents() {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }

    private suspend fun setupEvents() {
        welcomeMessageEvent()
        leaveMessageEvent()
    }

    private suspend fun setupSlashCommands() {
        broadcast()
        setting()
        welcomeMessage()
        leaveMessage()
    }

    private fun setupDatabase() {
        migrate(
            path = "classpath:db/administration/migration"
        )
    }

}