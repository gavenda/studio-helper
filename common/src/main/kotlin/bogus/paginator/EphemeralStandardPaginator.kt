package bogus.paginator

import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.SWITCH_EMOJI
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates by editing the given ephemeral interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
class EphemeralResponseStandardPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
    val interaction: EphemeralMessageInteractionResponseBehavior,
) : StandardPaginator(pages, owner, timeoutSeconds, true, switchEmoji, bundle, locale) {
    /** Whether this paginator has been set up for the first time. **/
    var embedFollowupMessage: EphemeralFollowupMessage? = null

    override suspend fun send() {
        if (embedFollowupMessage == null) {
            setup()

            embedFollowupMessage = interaction.createEphemeralFollowup {
                embed { applyPage() }

                with(this@EphemeralResponseStandardPaginator.components) {
                    this@createEphemeralFollowup.applyToMessage()
                }
            }
        } else {
            updateButtons()

            embedFollowupMessage?.edit {
                embed { applyPage() }

                with(this@EphemeralResponseStandardPaginator.components) {
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

        interaction.edit {
            embed { applyPage() }
            removeNavigationButtons()
            with(this@EphemeralResponseStandardPaginator.components) {
                this@edit.applyToMessage()
            }
        }

        super.destroy()
    }
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
fun EphemeralResponseStandardPaginator(
    builder: PaginatorBuilder,
    interaction: EphemeralMessageInteractionResponseBehavior
): EphemeralResponseStandardPaginator = EphemeralResponseStandardPaginator(
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    bundle = builder.bundle,
    locale = builder.locale,
    interaction = interaction,

    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
suspend inline fun EphemeralInteractionContext.editingStandardPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): EphemeralResponseStandardPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return EphemeralResponseStandardPaginator(pages, interactionResponse)
}
