package bogus.extension.music.paginator

import bogus.extension.music.player.MusicPlayer
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.allowedMentions
import dev.kord.rest.builder.message.modify.embed
import dev.kord.rest.request.RestRequestException
import java.util.*

/**
 * Class representing a button-based paginator that operates on standard messages.
 *
 * @param pingInReply Whether to ping the author of [targetMessage] in reply.
 * @param targetMessage Target message to reply to, overriding [targetChannel].
 * @param targetChannel Target channel to send the paginator to, if [targetMessage] isn't provided.
 */
class MessageQueuePaginator(
    player: MusicPlayer,
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    val pingInReply: Boolean = true,
    val targetChannel: MessageChannelBehavior? = null,
    val targetMessage: Message? = null,
) : QueuePaginator(player, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    init {
        if (targetChannel == null && targetMessage == null) {
            throw IllegalArgumentException("Must provide either a target channel or target message")
        }
    }

    /** Specific channel to send the paginator to. **/
    val channel: MessageChannelBehavior = targetMessage?.channel ?: targetChannel!!

    /** Message containing the paginator. **/
    var message: Message? = null

    override suspend fun send() {
        if (message == null) {
            setup()
            updateButtons()

            message = channel.createMessage {
                this.messageReference = targetMessage?.id

                allowedMentions { repliedUser = pingInReply }
                val pageNum = currentPageNum.coerceIn(0, pages.groups[""]?.size)

                currentPage = pages.get(pageNum)
                embed { applyPage() }

                with(this@MessageQueuePaginator.components) {
                    this@createMessage.applyToMessage()
                }
            }
        } else {
            updateButtons()

            message!!.edit {
                val pageNum = currentPageNum.coerceIn(0, pages.groups[""]?.size)
                currentPage = pages.get(pageNum)
                embed { applyPage() }

                with(this@MessageQueuePaginator.components) {
                    this@edit.applyToMessage()
                }
            }
        }
    }

    override suspend fun destroy() {
        if (!active) {
            return
        }

        active = false

        try {
            if (!keepEmbed) {
                message!!.delete()
            } else {
                message!!.edit {
                    allowedMentions { repliedUser = pingInReply }
                    embed { applyPage() }

                    this.components = mutableListOf()
                }
            }
        } catch (_: RestRequestException) {
        }

        super.destroy()
    }
}

/** Convenience function for creating a message button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
fun MessageQueuePaginator(
    player: MusicPlayer,
    pingInReply: Boolean = true,
    targetChannel: MessageChannelBehavior? = null,
    targetMessage: Message? = null,
    builder: MutablePaginatorBuilder
): MessageQueuePaginator =
    MessageQueuePaginator(
        player = player,
        pages = builder.pages,
        owner = builder.owner,
        timeoutSeconds = builder.timeoutSeconds,
        keepEmbed = builder.keepEmbed,
        bundle = builder.bundle,
        locale = builder.locale,

        pingInReply = pingInReply,
        targetChannel = targetChannel,
        targetMessage = targetMessage,

        switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    )

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
inline fun messageQueuePaginator(
    player: MusicPlayer,
    targetChannel: MessageChannelBehavior? = null,
    targetMessage: Message? = null,
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (MutablePaginatorBuilder).() -> Unit
): MessageQueuePaginator {
    val pages = MutablePaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return MessageQueuePaginator(player, targetChannel = targetChannel, targetMessage = targetMessage, builder = pages)
}
