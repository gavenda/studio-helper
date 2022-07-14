package bogus.extension.anilist.command.message

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.command.findMedia
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand

suspend fun AniListExtension.findMessageCommand() {
    publicMessageCommand {
        name = "command.find.message-command"
        action {
            findMedia(targetMessages.first().content)
        }
    }
}