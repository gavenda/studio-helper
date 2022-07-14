package bogus.extension.utility

import bogus.extension.utility.command.choose
import com.kotlindiscord.kord.extensions.extensions.Extension

class UtilityExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME
    override suspend fun setup() {
        configureIntents()
        setupCommands()
    }

    private fun configureIntents() {
    }

    private suspend fun setupCommands() {
        choose()
    }
}