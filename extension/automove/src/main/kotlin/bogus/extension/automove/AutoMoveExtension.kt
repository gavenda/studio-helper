package bogus.extension.automove

import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.gateway.Intent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging

class AutoMoveExtension(
    val defaultChannel: Snowflake,
    val deafChannel: Snowflake
) : Extension() {
    private val log = KotlinLogging.logger {  }
    override val name = "automove"
    override suspend fun setup() {
        intents += Intent.GuildVoiceStates

        event<VoiceStateUpdateEvent> {
            action {
                // Ignore self
                if (event.state.userId == kord.selfId) return@action
                val member = event.state.getMemberOrNull() ?: return@action
                // Check newly join
                if (event.old?.channelId == null) {
                    log.info { """msg="Joined voice channel" id=${member.id}""" }
                    autoMove(member)
                    return@action
                }
                // Ignore move updates
                if (event.old?.channelId != event.state.channelId) return@action
                autoMove(member)
            }
        }

        event<ReadyEvent> {
            action {
                val voiceChannel = kord.getChannelOf<VoiceChannel>(defaultChannel) ?: return@action
                val deafChannel = kord.getChannelOf<VoiceChannel>(deafChannel) ?: return@action

                voiceChannel.voiceStates.onEach {
                    val member = it.getMemberOrNull() ?: return@onEach
                    autoMove(it, member)
                }.collect()
                deafChannel.voiceStates.onEach {
                    val member = it.getMemberOrNull() ?: return@onEach
                    autoMove(it, member)
                }.collect()
            }
        }
    }

    suspend fun autoMove(voiceState: VoiceState, member: MemberBehavior) {
        if (voiceState.channelId != deafChannel && (voiceState.isSelfDeafened || voiceState.isSelfMuted)) {
            member.edit {
                voiceChannelId = deafChannel
            }
            log.info { """msg="Moving deafened member" id=${member.id}""" }
        } else if (voiceState.channelId != defaultChannel && !voiceState.isSelfDeafened && !voiceState.isSelfMuted) {
            member.edit {
                voiceChannelId = defaultChannel
            }
            log.info { """msg="Moving speaking member" id=${member.id}""" }
        }
    }

    suspend fun EventContext<VoiceStateUpdateEvent>.autoMove(member: MemberBehavior) {
        autoMove(event.state, member)
    }
}
