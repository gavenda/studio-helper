package bogus.extension.utility

import bogus.extension.utility.command.choose
import com.kotlindiscord.kord.extensions.extensions.Extension

class UtilityExtension : Extension() {
    override val name = "utility"
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