package bogus.extension.anilist.command.message

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.command.findCharacter
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand

suspend fun AniListExtension.characterMessageCommand() {
    publicMessageCommand {
        name = "command.character.message-command"
        action {
            findCharacter(targetMessages.first().content)
        }
    }
}