package bogus.extension.information.command

import bogus.extension.information.InformationExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.emoji
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed


suspend fun EphemeralSlashCommand<*>.emoji() {
    ephemeralSubCommand(::EmojiArgs) {
        name = "command.emoji"
        description = "command.emoji.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild?.asGuildOrNull() ?: return@action
            respond {
                embed {
                    author {
                        name = "Emoji Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    color = Color(InformationExtension.EMBED_COLOR)
                }
            }
        }
    }
}

private class EmojiArgs : Arguments() {
    val emoji by emoji {
        name = "command.emoji.args.emoji"
        description = "command.emoji.args.emoji.description"
    }
}