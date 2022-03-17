package bogus.extension.anilist.check

import com.kotlindiscord.kord.extensions.checks.failed
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.passed
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import mu.KotlinLogging

suspend fun CheckContext<*>.anyGuild(errorMessage: String = translate("checks.anyGuild.failed")) {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger {}

    if (guildFor(event) != null) {
        log.passed()
        pass()
    } else {
        log.failed("Event did not happen within a guild.")
        fail(errorMessage)
    }
}
