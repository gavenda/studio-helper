package bogus.extension.anilist.coroutines

import bogus.coroutines.Poller
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.AiringSchedule
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
    val log = KotlinLogging.logger { }

    // Empty, gets populated at first run
    private val mediaIdEpisode = mutableMapOf<Long, Int>()
    val aniList by inject<AniList>()
    val mediaIds get() = mediaIdEpisode.keys.toList()

    override fun poll(delay: Duration): Flow<List<AiringSchedule>> {
        return channelFlow {
            while (!isClosedForSend) {
                // Initial
                if (mediaIdList.isEmpty()) {
                    aniList.findAiringMedia(mediaIdList)?.forEach {
                        mediaIdEpisode[it.mediaId] = it.episode
                    }
                    send(emptyList())
                    return@channelFlow
                }
                delay(delay)
                val airingSchedules = aniList.findAiringMedia(mediaIdList)
                if (airingSchedules != null) {
                    send(updateMediaEpisodes(airingSchedules))
                } else {
                    send(emptyList())
                }
            }
        }.flowOn(coroutineDispatcher)
    }

    fun setupMediaIds(mediaIds: List<Long>) {
        mediaIds.forEach {
            mediaIdEpisode.putIfAbsent(it, 0)
        }

        log.info { """msg="Poller updated", mediaIds="$mediaIds"""" }
    }

    fun removeMediaId(mediaId: Long) {
        mediaIdEpisode.remove(mediaId)
    }

    /**
     * Updates the media episodes internally and returns only updated ones.
     */
    private fun updateMediaEpisodes(airingSchedule: List<AiringSchedule>): List<AiringSchedule> {
        val airingScheduleUpdate = mutableListOf<AiringSchedule>()
        airingSchedule.forEach {
            val episodeCount = mediaIdEpisode.getOrDefault(it.mediaId, 0)
            if (episodeCount < it.episode) {
                mediaIdEpisode[it.mediaId] = it.episode
                airingScheduleUpdate.add(it)
            }
        }
        return airingScheduleUpdate.toList()
    }

    override fun close() {
        coroutineDispatcher.cancel()
    }
}