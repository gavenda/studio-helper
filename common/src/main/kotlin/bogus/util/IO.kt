package bogus.util

import kotlinx.coroutines.*

/**
 * Loads any file in resources and returns them as a string.
 */
fun findResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path)!!.readText()
}

/**
 * Make a debounced function call.
 * @param waitMs time to wait before executing function
 * @param coroutineScope the coroutine scope
 * @param block the block to execute
 */
fun debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    block: suspend () -> Unit
): () -> Unit {
    var debounceJob: Job? = null
    return {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            block()
        }
    }
}