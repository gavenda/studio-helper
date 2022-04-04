package bogus.coroutines

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Simple interface for polling something in a hot flow.
 */
interface Poller<T> {
    /**
     * Poll every given delayed duration.
     * @param delay the duration to delay
     */
    fun poll(delay: Duration): Flow<T>
    fun close()
}