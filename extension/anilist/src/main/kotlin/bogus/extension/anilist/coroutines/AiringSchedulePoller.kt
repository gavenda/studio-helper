package bogus.extension.anilist.coroutines

import bogus.coroutines.Poller
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.AiringSchedule
import bogus.util.asLogFMT
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class AiringSchedulePoller(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val mediaIdList: List<Long>
) : KoinComponent, Poller<List<AiringSchedule>> {
    val log = KotlinLogging.logger { }.asLogFMT()

    // Empty, gets populated at first run
    private val mediaIdEpisode = mutableMapOf<Long, Int>()
    val aniList by inject<AniList>()
    val mediaIds get() = mediaIdEpisode.keys.toList()

    override fun poll(delay: Duration): Flow<List<AiringSchedule>> {
        runBlocking {
            aniList.findAiringMedia(mediaIdList)?.forEach {
                updateMediaEpisode(it.mediaId, it.episode)
            }

            log.debug("Initialized media list")
        }

        return channelFlow {
            while (!isClosedForSend) {
                log.debug("Fetching latest media")

                val airingSchedules = aniList.findAiringMedia(mediaIdList)
                if (airingSchedules != null) {
                    send(airingSchedules.filter { updateMediaEpisode(it.mediaId, it.episode) })
                } else {
                    send(emptyList())
                }

                delay(delay)
            }
        }.flowOn(coroutineDispatcher)
    }

    /**
     * Setup media ids
     * @param mediaIds the media ids
     */
    fun setupMediaIds(mediaIds: List<Long>) {
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

    override fun close() {
        coroutineDispatcher.cancel()
    }
}