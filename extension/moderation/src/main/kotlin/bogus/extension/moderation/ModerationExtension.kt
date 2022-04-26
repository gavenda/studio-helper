package bogus.extension.moderation

import bogus.extension.moderation.command.clean
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
class ModerationExtension : Extension() {
    override val name = "moderation"
    override suspend fun setup() {
        configureIntents()
        setupCommands()
    }

    private fun configureIntents() {
        intents += Intent.MessageContent
        intents += Intent.GuildMembers
    }

    private suspend fun setupCommands() {
        clean()
    }
}