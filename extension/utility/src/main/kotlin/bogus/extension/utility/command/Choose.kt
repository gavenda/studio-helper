package bogus.extension.utility.command

import bogus.extension.utility.UtilityExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun UtilityExtension.choose() {
    publicSlashCommand(::ChooseArgs) {
        name = "choose"
        description = "Lumi will choose an option for you!"
        check {
            anyGuild()
        }
        action {
            val choices = arguments.choices
                .split("|")
                .filter { it.isNotBlank() }
                .map { it.trim() }

            if (choices.isEmpty()) {
                respond {
                    content = "You did not add any choices!"
                }
            } else {
                val selectedChoice = choices.random()
                respond {
                    content = "I choose `$selectedChoice`."
                }
            }
        }
    }
}

private class ChooseArgs : Arguments() {
    val choices by coalescingString {
        name = "choices"
        description = "The choices to choose from, separated by '|'."
    }
}