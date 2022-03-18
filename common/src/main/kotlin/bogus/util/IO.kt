package bogus.util

import kotlinx.coroutines.*

/**
 * Loads any file in resources and returns them as a string.
 */
fun findResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path)!!.readText()
}

fun debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    destinationFunction: suspend () -> Unit
): () -> Unit {
    var debounceJob: Job? = null
    return {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction()
        }
    }
}