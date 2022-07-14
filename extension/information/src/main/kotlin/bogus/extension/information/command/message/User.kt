package bogus.extension.information.command.message

import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.extensions.ephemeralUserCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun InformationExtension.userMessageCommand() {
    ephemeralUserCommand {
        name = "Show User Info"
        action {
            respond {
                content = "Not yet implemented."
            }
        }
    }
}