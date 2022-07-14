package bogus.extension.information

import bogus.extension.information.command.info
import bogus.extension.information.command.message.userMessageCommand
import com.kotlindiscord.kord.extensions.extensions.Extension

class InformationExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME

    companion object {
        var EMBED_COLOR = 0
    }

    override suspend fun setup() {
        configureIntents()
        setupCommands()
        setupMessageCommands()
    }

    private fun configureIntents() {
    }

    private suspend fun setupCommands() {
        info()
    }

    private suspend fun setupMessageCommands() {
        userMessageCommand()
    }
}