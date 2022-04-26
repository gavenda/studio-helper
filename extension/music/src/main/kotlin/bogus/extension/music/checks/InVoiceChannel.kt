package bogus.extension.music.checks

import bogus.extension.music.TRANSLATION_BUNDLE
import bogus.extension.music.link
import bogus.extension.music.player
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasPermissions
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.connectAudio
import mu.KotlinLogging

suspend fun <T : Event> CheckContext<T>.inVoiceChannel() {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger { }.asLogFMT()
    val guild = guildFor(event)

    if (guild == null) {
        fail(translate("checks.anyGuild.failed"))
        return
    }

    val selfMember = guild.getMemberOrNull(event.kord.selfId)
    val member = memberFor(event)

    if (selfMember == null) {
        fail("Something happened in our end. Please contact the developer.")
        return
    }
    if (member == null) {
        fail("Something happened in our end. Please contact the developer.")
        return
    }

    val ourVoiceChannel = selfMember.getVoiceStateOrNull()?.getChannelOrNull()
    val theirVoiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()

    if (theirVoiceChannel == null) {
        log.debug("No voice channel")
        fail(translate("check.voice-channel.fail", TRANSLATION_BUNDLE))
        return
    }

    if (theirVoiceChannel != ourVoiceChannel) {
        val canTalk = selfMember.hasPermissions(
            Permission.Speak,
            Permission.Connect
        )
        if (!canTalk) {
            log.debug("No permission")
            fail(translate("check.voice-channel-permission.fail", TRANSLATION_BUNDLE))
            return
        }

        guild.player.assureConnection()
        guild.link.connectAudio(theirVoiceChannel.id)
        guild.player.effects.applyFilters()
        pass()
        return
    }

    if (theirVoiceChannel == ourVoiceChannel) {
        pass()
    } else {
        log.debug("Not in same channel")
        fail(translate("check.voice-channel-same.fail", TRANSLATION_BUNDLE))
    }

    if (guild.link.state != Link.State.CONNECTED) {
        guild.player.assureConnection()
        guild.link.connectAudio(theirVoiceChannel.id)
    }
}