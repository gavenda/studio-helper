package bogus.bot.lumi

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.Intent
import io.ktor.util.*

val BOGUS_ID = Snowflake(369435836627812352)
val NEIL_ID = Snowflake(240876177059741696)
val FILTERED_REGEX = Regex(
    "(good night|nite|sweet dreams|mayng buntag|good morning|morning|kiss|love|shua|justine|jahusa)"
)

class NeilBogoExtension : Extension() {
    override val name = "neil"

    override suspend fun setup() {
        intents += Intent.GuildMessages

        event<MessageCreateEvent> {
            action {
                val guildId = event.guildId
                val member = event.member

                if (guildId == null) return@action
                if (member == null) return@action
                if (guildId != BOGUS_ID) return@action
                if (member.id != NEIL_ID) return@action

                if (FILTERED_REGEX.containsMatchIn(event.message.content.toLowerCasePreservingASCIIRules())) {
                    event.message.delete("Not allowed for Neil")
                }
            }
        }
    }
}