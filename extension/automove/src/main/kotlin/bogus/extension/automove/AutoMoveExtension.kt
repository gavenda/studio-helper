package bogus.extension.automove

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.edit
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.gateway.Intent
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
                // Ignore move updates
                if (event.old?.channelId != event.state.channelId) return@action
                val member = event.state.getMemberOrNull() ?: return@action
                if (event.state.channelId != deafChannel && (event.state.isSelfDeafened || event.state.isSelfMuted)) {
                    member.edit {
                        voiceChannelId = deafChannel
                    }
                    return@action
                }
                if (event.state.channelId != defaultChannel) {
                    member.edit {
                        voiceChannelId = defaultChannel
                    }
                }
            }
        }
    }
}