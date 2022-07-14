package bogus.extension.information.command

import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand

suspend fun InformationExtension.info() {
    ephemeralSlashCommand {
        name = "command.info"
        description = "command.info.description"

        channel()
        emoji()
        role()
        server()
        user()
    }
}