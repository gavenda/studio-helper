package bogus.extension.music.check

import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

suspend fun <T : Event> CheckContext<T>.hasDJRole() {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger { }
    val user = userFor(event)
    val guild = guildFor(event)

    if (user == null) {
        log.failed("No user")
        fail()
    } else {
        val role = guild?.roles?.firstOrNull {
            it.name == "DJ"
        }
        val member = guild?.getMember(user.id) ?: return

        if (role == null) {
            log.passed()
            pass()
        } else if (member.roles.toList().contains(role)) {
            log.passed()
            pass()
        } else {
            log.failed("No DJ role")
            fail(translate("check.dj.fail", "music", arrayOf(role.mention)))
        }
    }
}