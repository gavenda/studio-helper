package bogus.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.ComponentContext
import com.kotlindiscord.kord.extensions.components.ComponentWithAction
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.events.EventContext
import com.kotlindiscord.kord.extensions.events.EventHandler
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Call this to supply a command body with a specified dispatcher, to be called when the command is executed.
 */
inline fun <C : SlashCommandContext<C, A, M>, A : Arguments, M : ModalForm> SlashCommand<C, A, M>.action(
    dispatcher: CoroutineDispatcher,
    crossinline action: suspend C.() -> Unit
) {
    action {
        CoroutineScope(dispatcher).launch {
            action()
        }
    }
}

/**
 * Call this to supply a command body with a specified dispatcher, to be called when the command is executed.
 */
inline fun <T : Event> EventHandler<T>.action(
    dispatcher: CoroutineDispatcher,
    crossinline action: suspend EventContext<T>.() -> Unit
) {
    action {
        CoroutineScope(dispatcher).launch {
            action()
        }
    }
}

/**
 * Call this to supply a command body with a specified dispatcher, to be called when the command is executed.
 */
inline fun <E : ComponentInteractionCreateEvent, C : ComponentContext<*>, M : ModalForm> ComponentWithAction<E, C, M>.action(
    dispatcher: CoroutineDispatcher,
    crossinline action: suspend C.() -> Unit
) {
    action {
        CoroutineScope(dispatcher).launch {
            action()
        }
    }
}

