package bogus.checks

import com.kotlindiscord.kord.extensions.checks.failed
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.checks.userFor
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val guildLimits = mutableMapOf<Snowflake, MutableMap<Snowflake, MutableMap<String, Long>>>()

suspend fun CheckContext<ApplicationCommandInteractionCreateEvent>.limit(duration: Duration) {
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

    val user = userFor(event)

    if (user == null) {
        log.failed("No user")
        throw UnsupportedOperationException("Cannot use with non-user events")
    }

    val userLimits = guildLimits.getOrPut(guild.id) {
        mutableMapOf()
    }
    val userLimit = userLimits.getOrPut(user.id) {
        mutableMapOf()
    }
    val userLimitMillis = userLimit.getOrPut(event.interaction.invokedCommandName) {
        System.currentTimeMillis()
    }
    val deltaMillis = System.currentTimeMillis() - userLimitMillis

    if (deltaMillis < duration.inWholeMilliseconds) {
        fail("You can only use `${event.interaction.invokedCommandName}` every ${duration.inWholeMinutes} minutes. You have ${deltaMillis.minutes.inWholeMinutes} minute(s) left.")
        return
    }

    userLimit[event.interaction.invokedCommandName] = System.currentTimeMillis()
    pass()
}