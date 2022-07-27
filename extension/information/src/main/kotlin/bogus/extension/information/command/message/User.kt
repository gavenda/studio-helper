package bogus.extension.information.command.message

import bogus.extension.information.InformationExtension
import bogus.extension.information.command.showUserInformation
import com.kotlindiscord.kord.extensions.extensions.ephemeralUserCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun InformationExtension.userMessageCommand() {
    ephemeralUserCommand {
        name = "Show User Info"
        action {
            showUserInformation(targetUsers.first())
        }
    }
}