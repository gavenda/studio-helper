package bogus.extension.music.check

import bogus.extension.music.link
import bogus.extension.music.player
import com.kotlindiscord.kord.extensions.checks.failed
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.passed
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasPermissions
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import dev.schlaubi.lavakord.kord.connectAudio
import mu.KotlinLogging

suspend fun <T : Event> CheckContext<T>.inVoiceChannel() {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger { }
    val guild = guildFor(event)?.fetchGuild()

    if (guild == null) {
        log.failed("No guild")
        fail(translate("checks.anyGuild.failed"))
        return
    }

    val selfMember = guild.getMember(event.kord.selfId)
    val member = memberFor(event)?.fetchMember()

    if (member == null) {
        log.failed("No member")
        fail()
        return
    }

    val ourVoiceChannel = selfMember.getVoiceStateOrNull()?.getChannelOrNull()
    val theirVoiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()

    if (theirVoiceChannel == null) {
        log.failed("No voice channel")
        fail(translate("check.voice-channel.fail", "music"))
        return
    }

    if (theirVoiceChannel != ourVoiceChannel) {
        val canTalk = selfMember.hasPermissions(
            Permission.Speak,
            Permission.Connect
        )
        if (!canTalk) {
            log.failed("No permission")
            fail(translate("check.voice-channel-permission.fail", "music"))
            return
        }

        guild.player.assureConnection()
        guild.link.connectAudio(theirVoiceChannel.id)
        guild.player.effects.applyFilters()
    }

    log.passed()
    pass()
}