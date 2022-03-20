package bogus.bot.vivy

import bogus.extension.music.Jukebox
import bogus.extension.music.check.hasDJRole
import bogus.extension.music.check.inVoiceChannel
import bogus.extension.music.player
import bogus.util.action
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging

class SingExtension : Extension() {
    override val name: String = "sing"
    private val log = KotlinLogging.logger { }
    private val singMutex = Mutex()
    private val songList = listOf(
        "https://www.youtube.com/watch?v=JqN4_mufE2U",
        "https://www.youtube.com/watch?v=wi8sbvZ0CQE2",
        "https://www.youtube.com/watch?v=CmW8jQTI5N0",
        "https://www.youtube.com/watch?v=fZkze8cKHgI",
        "https://www.youtube.com/watch?v=nbSwgEWkM6w",
        "https://www.youtube.com/watch?v=mHmB5mhkuP0",
        "https://www.youtube.com/watch?v=2qcvp5nE4Lc"
    )

    override suspend fun setup() {
        ephemeralSlashCommand {
            name = "sing"
            description = "Vivy will sing you her songs."
            check {
                inVoiceChannel()
                hasDJRole()
            }
            action(Dispatchers.IO) {
                val guild = guild ?: return@action
                // Fire and forget, but maintain order
                CoroutineScope(Dispatchers.IO).launch {
                    songList.forEach {
                        singMutex.withLock {
                            Jukebox.playLaterSilently(
                                identifier = it,
                                guild = guild,
                                mention = user.mention,
                                userId = user.id
                            )
                        }
                    }
                }

                delay(2000)
                guild.player.attemptToPlay()

                log.info { """msg="Requested songs" user=${user.id}""" }

                respond {
                    content = "I will now sing you my songs."
                }
            }
        }
    }
}