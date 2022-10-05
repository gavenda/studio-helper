package bogus.extension.anilist.coroutines

import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.AiringSchedule
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.inject

data class AiringScheduleMedia(
    val mediaId: Long,
    val userId: Long
)

class AiringSchedulePoller(
    private val medias: List<AiringScheduleMedia>
) : KordExKoinComponent {
    val log = KotlinLogging.logger { }.asFMTLogger()

    // Empty, gets populated at first run
    private val mediaIdEpisode = mutableMapOf<Long, Int>()
    val aniList by inject<AniList>()
    val mediaIds get() = medias.map { it.mediaId }

    init {
        runBlocking {
            aniList.findAiringMedia(mediaIds)?.forEach {
                updateMediaEpisode(it.mediaId, it.episode)
            }
            log.debug { message = "Initialized media list" }
        }
    }

    fun isEmpty(): Boolean = mediaIdEpisode.keys.isEmpty()

    fun requestingUserId(mediaId: Long): Long {
        return medias.first { it.mediaId == mediaId }.userId
    }

    suspend fun poll(): List<AiringSchedule> {
        val airingSchedules = try {
            aniList.findAiringMedia(mediaIds)
        } catch (e: Exception) {
            log.error(e) {
                message = "Error retrieving airing medias"
                context = mapOf(
                    "error" to e.message
                )
            }
            emptyList()
        }
        return airingSchedules?.filter { updateMediaEpisode(it.mediaId, it.episode) } ?: emptyList()
    }

    /**
     * Update media ids
     * @param mediaIds the media ids
     */
    fun updateMediaIds(mediaIds: List<Long>) {
        mediaIds.forEach {
            mediaIdEpisode.putIfAbsent(it, 0)
        }

        log.debug { message = "Poller updated" }
    }

    /**
     * Remove media id
     * @param mediaId the media id to remove
     */
    fun removeMediaId(mediaId: Long) {
        mediaIdEpisode.remove(mediaId)
    }

    /**
     * Updates media episode if it is lesser than previously recorded.
     * @return true if updated, false otherwise
     */
    private fun updateMediaEpisode(mediaId: Long, episode: Int): Boolean {
        val episodeCount = mediaIdEpisode.getOrDefault(mediaId, 0)
        if (episodeCount < episode) {
            mediaIdEpisode[mediaId] = episode
            return true
        }
        return false
    }
}