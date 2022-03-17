package bogus.extension.music

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.kord.getLink
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of guild links and track metadata.
 */
object Lava : KoinComponent {
    private val trackMetas = ConcurrentHashMap<Track, AudioTrackMeta>()
    private val lavaKord by inject<LavaKord>()

    /**
     * Returns a [Link] for the given guild snowflake.
     * @param guildId the guild snowflake
     */
    fun linkFor(guildId: Snowflake): Link {
        return lavaKord.getLink(guildId)
    }

    /**
     * Returns a [Link] for the given guild behaviour.
     * @param guild the guild behaviour
     */
    fun linkFor(guild: GuildBehavior): Link {
        return lavaKord.getLink(guild.id)
    }

    /**
     * Retrieves a metadata for this track.
     * @param track the track to retrieve a metadata from
     */
    fun metaFor(track: Track): AudioTrackMeta? {
        return trackMetas[track]
    }

    /**
     * Attaches a meta for this track.
     * @param track the track to attach a meta to
     * @param meta the audio track metadata
     */
    fun attachMeta(track: Track, meta: AudioTrackMeta) {
        trackMetas.computeIfAbsent(track) { meta }
    }
}