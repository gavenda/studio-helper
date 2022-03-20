package bogus.extension.music.check

import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.hasRole
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
    val role = guild?.roles?.firstOrNull { it.name == "DJ" }

    if (role == null) {
        log.passed()
        pass()
        return
    }

    if (user == null) {
        fail()
        return
    }

    val member = guild.getMemberOrNull(user.id)

    if (member == null) {
        log.nullMember(event)
        fail()
        return
    }

    if (member.hasRole(role)) {
        log.passed()
        pass()
        return
    }

    log.failed("""msg="no dj role" member="$member"""")

    fail(
        translate(
            key = "check.dj.fail",
            replacements = arrayOf(role.mention),
        )
    )
}