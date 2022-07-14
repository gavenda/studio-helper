package bogus.extension.anilist.command.message

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.command.findStaff
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand

suspend fun AniListExtension.staffMessageCommand() {
    publicMessageCommand {
        name = "Search Staff"
        action {
            findStaff(targetMessages.first().content)
        }
    }
}