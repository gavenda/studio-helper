package bogus.extension.information.command.message

import bogus.extension.information.InformationExtension
import bogus.extension.information.command.showUserInformation
import com.kotlindiscord.kord.extensions.extensions.ephemeralUserCommand

suspend fun InformationExtension.userMessageCommand() {
    ephemeralUserCommand {
        name = "Show User Info"
        action {
            showUserInformation(targetUsers.first())
        }
    }
}