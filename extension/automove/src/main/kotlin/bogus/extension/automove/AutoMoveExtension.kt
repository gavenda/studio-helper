package bogus.extension.automove

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.gateway.Intent

class AutoMoveExtension(
    val DEFAULT_CHANNEL: Snowflake,
    val DEAF_CHANNEL: Snowflake
) : Extension() {
    override val name = "automove"
    override suspend fun setup() {
        intents += Intent.GuildVoiceStates

        event<VoiceStateUpdateEvent> {
            action {
                val member = event.state.getMemberOrNull() ?: return@action
                val guild = event.state.getGuildOrNull() ?: return@action
                if (event.state.isSelfDeafened || event.state.isSelfMuted) {
                    val defaultChannel = guild.getChannelOfOrNull<VoiceChannel>(DEFAULT_CHANNEL) ?: return@action

                    return@action
                }
                if (event.state.channelId == null) return@action
                if (event.state.channelId != DEFAULT_CHANNEL) {

                }
            }
        }
    }
}