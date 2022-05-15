package bogus.extension.music.paginator

import bogus.extension.music.*
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.player.MusicPlayer
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
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import java.util.*

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
abstract class QueuePaginator(
    val player: MusicPlayer,
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

    val numberOfPages get() = pages.groups[currentGroup]?.size?.minus(1) ?: 0

    open var playPauseButton: PublicInteractionButton? = null
    open var skipButton: PublicInteractionButton? = null
    open var shuffleButton: PublicInteractionButton? = null
    open var volumeUpButton: PublicInteractionButton? = null
    open var volumeDownButton: PublicInteractionButton? = null
    open var repeatSingleButton: PublicInteractionButton? = null
    open var repeatAllButton: PublicInteractionButton? = null
    open var stopButton: PublicInteractionButton? = null

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
                emoji(EmojiPlay)
            } else {
                emoji(EmojiPause)
            }

            check {
                anyGuild()
                hasDJRole()
            }

            action {
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
            emoji(EmojiSkip)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                player.skip()
                task?.restart()
            }
        }

        shuffleButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(EmojiShuffle)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                player.shuffle()
                task?.restart()
            }
        }

        volumeDownButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(EmojiVolumeDown)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                val volume = (player.volume - 10).coerceIn(0, 100)
                player.volumeTo(volume)
                task?.restart()
            }
        }

        volumeUpButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(EmojiVolumeUp)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                val volume = (player.volume + 10).coerceIn(0, 100)
                player.volumeTo(volume)
                task?.restart()
            }
        }

        repeatAllButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(EmojiRepeatAll)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                player.toggleLoopAll()
                task?.restart()
            }
        }

        repeatSingleButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Secondary
            emoji(EmojiRepeatSingle)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                player.toggleLoop()
                task?.restart()
            }
        }

        stopButton = components.publicButton {
            deferredAck = true
            style = ButtonStyle.Danger
            emoji(EmojiStop)

            check {
                anyGuild()
                hasDJRole()
            }

            action {
                player.clear()
                player.stop()
                task?.restart()
            }
        }

        addNavigationButtons()

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

        if (page < 0 || page > numberOfPages) {
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
            emoji(EmojiPrev)

            action {
                if (currentPageNum == 0) {
                    goToPage(numberOfPages)
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
            emoji(EmojiNext)

            action {
                if (currentPageNum >= numberOfPages) {
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
    fun updateButtons() {
        if (player.paused) {
            playPauseButton?.emoji(EmojiPlay)
        } else {
            playPauseButton?.emoji(EmojiPause)
        }

        if (player.loopedAll) {
            repeatAllButton?.emoji(EmojiRepeatAllOn)
        } else {
            repeatAllButton?.emoji(EmojiRepeatAll)
        }

        if (player.looped) {
            repeatSingleButton?.emoji(EmojiRepeatSingleOn)
        } else {
            repeatSingleButton?.emoji(EmojiRepeatSingle)
        }

        if (numberOfPages > 0) {
            nextButton?.enable()
            backButton?.enable()
        } else {
            nextButton?.disable()
            backButton?.disable()
        }
    }
}
