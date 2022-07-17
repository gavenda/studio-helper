package bogus.extension.listenmoe.command

import bogus.extension.listenmoe.AniRadioExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.annotation.KordVoice

@OptIn(KordVoice::class)
suspend fun AniRadioExtension.disconnect() {
    ephemeralSlashCommand {
        name = "command.disconnect"
        description = "command.disconnect.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val radio = radioByGuild(guild.id)

            radio.voiceConnection?.apply {
                leave()
                radio.voiceConnection = null
                respond {
                    content = translate("response.disconnect")
                }
            } ?: respond {
                content = translate("response.disconnect.not-connected")
            }
        }
    }
}