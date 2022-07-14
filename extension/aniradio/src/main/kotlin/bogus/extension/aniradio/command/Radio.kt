package bogus.extension.aniradio.command

import bogus.extension.aniradio.AniRadioExtension
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermissions
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.connect

@OptIn(KordVoice::class)
suspend fun AniRadioExtension.radio() {
    ephemeralSlashCommand {
        name = "command.radio"
        description = "command.radio.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val selfMember = guild.getMemberOrNull(event.kord.selfId)
            val member = memberFor(event)

            if (selfMember == null) {
                respond {
                    content = translate("checks.voice-channel.error")
                }
                return@action
            }
            if (member == null) {
                respond {
                    content = translate("checks.voice-channel.error")
                }
                return@action
            }

            val ourVoiceChannel = selfMember.getVoiceStateOrNull()?.getChannelOrNull()
            val theirVoiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()

            if (theirVoiceChannel == null) {
                log.debug {
                    message = "No voice channel"
                }
                respond {
                    content = translate("checks.voice-channel.not-in-voice")
                }
                return@action
            }

            if (theirVoiceChannel != ourVoiceChannel) {
                val canTalk = selfMember.hasPermissions(
                    Permission.Speak,
                    Permission.Connect
                )
                if (!canTalk) {
                    log.debug {
                        message = "No permission"
                    }
                    respond {
                        content = translate("checks.voice-channel.no-permission")
                    }
                    return@action
                }

                // Connect
                voiceConnection = theirVoiceChannel.connect {
                    audioProvider { audioProvider() }
                }
            }

            respond {
                content = translate("response.radio")
            }

            playAudio()
        }
    }
}