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
import mu.KotlinLogging

class AutoMoveExtension(
    val defaultChannel: Snowflake,
    val deafChannel: Snowflake
) : Extension() {
    private val log = KotlinLogging.logger { }
    override val name = "automove"
    override suspend fun setup() {
        if (deafChannel == Snowflake(0L) || defaultChannel == Snowflake(0L)) {
            log.info { "Auto move disabled, deaf channel or default channel is zero" }
            return
        }

        intents += Intent.GuildVoiceStates

        event<VoiceStateUpdateEvent> {
            action {
                // Ignore self
                if (event.state.userId == kord.selfId) return@action
                val member = event.state.getMemberOrNull() ?: return@action
                autoMove(member)
            }
        }

        event<ReadyEvent> {
            action {
                val voiceChannel = kord.getChannelOf<VoiceChannel>(defaultChannel) ?: return@action
                val deafChannel = kord.getChannelOf<VoiceChannel>(deafChannel) ?: return@action

                voiceChannel.voiceStates.collect {
                    val member = it.getMemberOrNull() ?: return@collect
                    autoMove(it, member)
                }
                deafChannel.voiceStates.collect {
                    val member = it.getMemberOrNull() ?: return@collect
                    autoMove(it, member)
                }
            }
        }
    }

    suspend fun autoMove(voiceState: VoiceState, member: MemberBehavior) {
        // Ignore if not both default channels
        if (!(voiceState.channelId == defaultChannel || voiceState.channelId == deafChannel)) return
        if (voiceState.channelId != deafChannel && voiceState.isSelfDeafened) {
            member.edit {
                voiceChannelId = deafChannel
            }
            log.info { "Moving deafened member [ memberId = ${member.id} ]" }
        } else if (voiceState.channelId != defaultChannel && !voiceState.isSelfDeafened) {
            member.edit {
                voiceChannelId = defaultChannel
            }
            log.info { "Moving speaking member [ memberId = ${member.id} ]" }
        }
    }

    suspend fun EventContext<VoiceStateUpdateEvent>.autoMove(member: MemberBehavior) {
        autoMove(event.state, member)
    }
}
