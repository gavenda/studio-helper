package bogus.extension.music

import dev.kord.common.entity.Snowflake
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.kord.getLink
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Keeps track of guild links and track metadata.
 */
object Lava : KoinComponent {
    private val lavaKord by inject<LavaKord>()

    /**
     * Returns a [Link] for the given guild snowflake.
     * @param guildId the guild snowflake
     */
    fun linkFor(guildId: Snowflake): Link {
        return lavaKord.getLink(guildId)
    }
}