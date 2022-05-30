package bogus.extension.anilist.coroutines

import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.AiringSchedule
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.inject

class AiringSchedulePoller(
    private val mediaIdList: List<Long>
) : KordExKoinComponent {
    val log = KotlinLogging.logger { }.asLogFMT()

    // Empty, gets populated at first run
    private val mediaIdEpisode = mutableMapOf<Long, Int>()
    val aniList by inject<AniList>()
    val mediaIds get() = mediaIdEpisode.keys.toList()

    init {
        runBlocking {
            aniList.findAiringMedia(mediaIdList)?.forEach {
                updateMediaEpisode(it.mediaId, it.episode)
            }
            log.debug("Initialized media list")
        }
    }

    suspend fun poll(): List<AiringSchedule> {
        val airingSchedules = aniList.findAiringMedia(mediaIdList)
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

        log.debug("Poller updated")
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