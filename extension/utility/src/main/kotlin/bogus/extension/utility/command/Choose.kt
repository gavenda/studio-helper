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
        name = "command.choose"
        description = "command.choose.description"
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
                    content = translate("response.choose.no-choices")
                }
            } else {
                val selectedChoice = choices.random()
                respond {
                    content = translate("response.choose", arrayOf(selectedChoice))
                }
            }
        }
    }
}

private class ChooseArgs : Arguments() {
    val choices by coalescingString {
        name = "command.choose.args.choices"
        description = "command.choose.args.choices.description"
    }
}