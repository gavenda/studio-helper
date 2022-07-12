package bogus.util

import dev.kord.core.Kord
import kotlinx.coroutines.runBlocking

/**
 * Requests to retrieve the bot avatar url.
 */
val Kord.selfAvatarUrl: String
    get() = runBlocking {
        getSelf().avatar?.cdnUrl?.toUrl() ?: ""
    }