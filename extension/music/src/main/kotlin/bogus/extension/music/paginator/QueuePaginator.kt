package bogus.extension.music.paginator

import bogus.extension.music.GuildMusicPlayer
import bogus.extension.music.check.hasDJRole
import bogus.util.action
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.pagination.BasePaginator
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
abstract class QueuePaginator(
    val player: GuildMusicPlayer,
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
) : BasePaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** [ComponentContainer] instance managing the buttons for this paginator. **/
    var components: ComponentContainer = ComponentContainer()

    /** Scheduler used to schedule the paginator's timeout. **/
    var scheduler: Scheduler = Scheduler()

    /** Scheduler used to schedule the paginator's timeout. **/
    var task: Task? = if (timeoutSeconds != null) {
        scheduler.schedule(timeoutSeconds) { destroy() }
    } else {
        null
    }

    private val emojiPlay = ReactionEmoji.Custom(Snowflake(904451990623514645), "play", false)
    private val emojiSkip = ReactionEmoji.Custom(Snowflake(904451990225051701), "skip", false)
    private val emojiPause = ReactionEmoji.Custom(Snowflake(904469954215157771), "pause", false)
    private val emojiShuffle = ReactionEmoji.Custom(Snowflake(904477192283631627), "shuffle", false)
    private val emojiNext = ReactionEmoji.Custom(Snowflake(905638284468830229), "next", false)
    private val emojiPrev = ReactionEmoji.Custom(Snowflake(905638284074557461), "previous", false)

    open var playPauseButton: PublicInteractionButton? = null
    open var skipButton: PublicInteractionButton? = null
    open var shuffleButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the previous page. **/
    open var backButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the next page. **/
    open var nextButton: PublicInteractionButton? = null

    override suspend fun destroy() {
        runTimeoutCallbacks()
        task?.cancel()
    }

    override suspend fun setup() {
        // Add play buttons...
        playPauseButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary

            if (player.paused) {
                emoji(emojiPlay)
            } else {
                emoji(emojiPause)
            }

            check {
                anyGuild()
                hasDJRole()
            }

            action(Dispatchers.IO) {
                if (player.paused) {
                    player.resume()
                } else {
                    player.pause()
                }

                task?.restart()
            }
        }

        skipButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(emojiSkip)

            check {
                anyGuild()
                hasDJRole()
            }

            action(Dispatchers.IO) {
                player.skip()
                task?.restart()
            }
        }

        shuffleButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(emojiShuffle)

            check {
                anyGuild()
                hasDJRole()
            }

            action(Dispatchers.IO) {
                player.shuffle()
                task?.restart()
            }
        }


        if (pages.groups.values.any { it.size > 1 }) {
            addNavigationButtons()
        }

        components.sort()
    }

    /**
     * Convenience function to switch to a specific group.
     */
    suspend fun switchGroup(group: String) {
        if (group == currentGroup) {
            return
        }

        // To avoid out-of-bounds
        currentPageNum = minOf(currentPageNum, pages.groups[group]!!.size)
        currentPage = pages.get(group, currentPageNum)
        currentGroup = group

        send()
    }

    override suspend fun nextGroup() {
        val current = currentGroup
        val nextIndex = allGroups.indexOf(current) + 1

        if (nextIndex >= allGroups.size) {
            switchGroup(allGroups.first())
        } else {
            switchGroup(allGroups[nextIndex])
        }
    }

    override suspend fun goToPage(page: Int) {
        if (page == currentPageNum) {
            return
        }

        if (page < 0 || page > pages.groups[currentGroup]!!.size - 1) {
            return
        }

        currentPageNum = page
        currentPage = pages.get(currentGroup, currentPageNum)

        send()
    }

    private suspend fun addNavigationButtons() {
        // Add navigation buttons...
        backButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Primary
            emoji(emojiPrev)

            action(Dispatchers.IO) {
                if (currentPageNum == 0) {
                    goToPage(pages.groups[currentGroup]!!.size - 1)
                } else {
                    previousPage()
                }

                send()
                task?.restart()
            }
        }

        nextButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Primary
            emoji(emojiNext)

            action(Dispatchers.IO) {
                if (currentPageNum >= pages.groups[currentGroup]!!.size - 1) {
                    goToPage(0)
                } else {
                    nextPage()
                }

                send()
                task?.restart()
            }
        }
    }

    /**
     * Convenience function that enables and disables buttons as necessary, depending on the current page number.
     */
    suspend fun updateButtons() {
        if (player.paused) {
            playPauseButton?.emoji(emojiPlay)
        } else {
            playPauseButton?.emoji(emojiPause)
        }

        if (pages.groups.values.any { it.size > 1 }) {
            if (backButton == null && nextButton == null) {
                addNavigationButtons()
                components.sort()
            }
        } else {
            backButton?.let { components.remove(it) }
            nextButton?.let { components.remove(it) }
            backButton = null
            nextButton = null
        }
    }
}
