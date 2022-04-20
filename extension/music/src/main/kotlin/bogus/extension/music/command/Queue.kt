package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.player
import bogus.paginator.editingStandardPaginator
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand

suspend fun MusicExtension.queue() {
    ephemeralSlashCommand {
        name = "queue"
        description = "queue.description"
        check {
            anyGuild()
        }
        action {
            val paginator = editingStandardPaginator {
                val embedBuilders = player.buildQueueMessage()
                for (embedBuilder in embedBuilders) {
                    page { embedBuilder() }
                }
            }

            paginator.send()
        }
    }
}