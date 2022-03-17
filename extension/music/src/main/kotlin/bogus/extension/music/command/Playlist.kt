package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.check.inVoiceChannel
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.Dispatchers

suspend fun MusicExtension.playlist() {
    ephemeralSlashCommand {
        name = "playlist"
        description = "playlist.description"

        list()
        show()
        create()
        delete()
        add()
        remove()
        queue()
    }
}

private suspend fun EphemeralSlashCommand<*>.list() {
    ephemeralSubCommand {
        name = "list"
        description = "playlist.list.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.show() {
    ephemeralSubCommand {
        name = "show"
        description = "playlist.show.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.create() {
    ephemeralSubCommand {
        name = "create"
        description = "playlist.create.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.delete() {
    ephemeralSubCommand {
        name = "delete"
        description = "playlist.delete.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.add() {
    ephemeralSubCommand {
        name = "add"
        description = "playlist.add.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.remove() {
    ephemeralSubCommand {
        name = "remove"
        description = "playlist.remove.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.queue() {
    ephemeralSubCommand {
        name = "queue"
        description = "playlist.queue.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action(Dispatchers.IO) {
            respond { content = "Not implemented." }
        }
    }
}