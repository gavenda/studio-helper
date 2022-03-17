package bogus.extension.anilist.paginator

import bogus.extension.anilist.failSilently
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.FollowupPermittingInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates by creating and editing a follow-up message for the
 * given public interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
class PublicFollowUpStandardPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,

    val interaction: FollowupPermittingInteractionResponseBehavior,
) : StandardPaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** Follow-up interaction to use for this paginator's embeds. Will be created by [send]. **/
    var embedInteraction: PublicFollowupMessage? = null

    override suspend fun send() {
        if (embedInteraction == null) {
            setup()

            embedInteraction = interaction.createPublicFollowup {
                embed {
                    applyPage()
                    url?.let { addLinkButton(it) }
                }

                with(this@PublicFollowUpStandardPaginator.components) {
                    this@createPublicFollowup.applyToMessage()
                }
            }
        } else {
            updateButtons()

            embedInteraction!!.edit {
                embed { applyPage() }

                with(this@PublicFollowUpStandardPaginator.components) {
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
        failSilently {
            if (!keepEmbed) {
                embedInteraction?.delete()
            } else {
                embedInteraction?.edit {
                    embed { applyPage() }
                    components = mutableListOf()
                }
            }
        }

        super.destroy()
    }
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
fun PublicFollowUpStandardPaginator(
    builder: PaginatorBuilder,
    interaction: FollowupPermittingInteractionResponseBehavior
): PublicFollowUpStandardPaginator = PublicFollowUpStandardPaginator(
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    keepEmbed = builder.keepEmbed,
    bundle = builder.bundle,
    locale = builder.locale,
    interaction = interaction,

    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)

/** Create a paginator that creates a follow-up message, and edits that. **/
suspend inline fun PublicInteractionContext.respondingStandardPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): PublicFollowUpStandardPaginator {
    val pages = PaginatorBuilder(
        locale = locale,
        defaultGroup = defaultGroup
    )

    builder(pages)

    return PublicFollowUpStandardPaginator(pages, interactionResponse)
}
