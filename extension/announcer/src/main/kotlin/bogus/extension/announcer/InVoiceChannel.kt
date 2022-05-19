package bogus.extension.announcer

import com.kotlindiscord.kord.extensions.checks.failed
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasPermissions
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import dev.kord.voice.AudioFrame
import mu.KotlinLogging

@OptIn(KordVoice::class)
suspend fun <T : Event> CheckContext<T>.inVoiceChannel(audioProvider: () -> AudioFrame?) {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger { }
    val guild = guildFor(event)

    if (guild == null) {
        log.failed("No guild")
        fail(translate("checks.anyGuild.failed"))
        return
    }

    val selfMember = guild.getMemberOrNull(event.kord.selfId)
    val member = memberFor(event)

    if (selfMember == null) {
        fail(translate("checks.voiceChannel.error", TRANSLATIONS_BUNDLE))
        return
    }
    if (member == null) {
        fail(translate("checks.voiceChannel.error", TRANSLATIONS_BUNDLE))
        return
    }

    val ourVoiceChannel = selfMember.getVoiceStateOrNull()?.getChannelOrNull()
    val theirVoiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()

    if (theirVoiceChannel == null) {
        log.failed("No voice channel")
        fail(translate("checks.voiceChannel.notInVoice", TRANSLATIONS_BUNDLE))
        return
    }

    if (theirVoiceChannel != ourVoiceChannel) {
        val canTalk = selfMember.hasPermissions(
            Permission.Speak,
            Permission.Connect
        )
        if (!canTalk) {
            log.failed("No permission")
            fail(translate("checks.voiceChannel.noPermission", TRANSLATIONS_BUNDLE))
            return
        }

        // Connect
        theirVoiceChannel.connect {
            audioProvider { audioProvider() }
        }
    }
}