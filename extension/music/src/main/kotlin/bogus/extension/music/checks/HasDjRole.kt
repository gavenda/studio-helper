package bogus.extension.music.checks

import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.core.event.Event
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging

suspend fun <T : Event> CheckContext<T>.hasDJRole() {
    if (!passed) {
        return
    }

    val log = KotlinLogging.logger { }.asLogFMT()
    val user = userFor(event)
    val guild = guildFor(event)
    val role = guild?.roles?.firstOrNull { it.name == "DJ" }

    if (role == null) {
        pass()
        return
    }

    if (user == null) {
        fail()
        return
    }

    val member = guild.getMemberOrNull(user.id)

    if (member == null) {
        fail()
        return
    }

    if (member.hasRole(role)) {
        pass()
        return
    }

    log.debug("No dj role", mapOf("memberId" to member.id))

    fail(
        translate(
            key = "check.dj.fail",
            replacements = arrayOf(role.mention),
        )
    )
}