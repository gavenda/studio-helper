package bogus.extension.aniradio

import bogus.util.asFMTLogger
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import dev.kord.common.annotation.KordVoice
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import io.ktor.util.*
import mu.KotlinLogging
import java.nio.ByteBuffer

@OptIn(KordVoice::class)
class RadioPlayer(
    val playerManager: AudioPlayerManager
) {
    val log = KotlinLogging.logger { }.asFMTLogger()
    var voiceConnection: VoiceConnection? = null
    val buffer: ByteBuffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }
    val player: AudioPlayer = playerManager.createPlayer()
    var radioType = RadioType.JPOP

    fun playAudio(type: RadioType) {
        radioType = type
        val radioUri = if (type == RadioType.JPOP) JPOP_RADIO_URI else KPOP_RADIO_URI

        playerManager.loadItem(radioUri, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = player.playTrack(track)
            override fun playlistLoaded(playlist: AudioPlaylist) {}
            override fun noMatches() {}
            override fun loadFailed(exception: FriendlyException) {
                log.error {
                    message = "Audio file failed to load"
                    context = mapOf(
                        "reason" to exception.message
                    )
                }
            }
        })
    }

    val audioProvider: () -> AudioFrame? = {
        val canProvide = player.provide(frame)
        if (canProvide) {
            AudioFrame.fromData(buffer.flip().moveToByteArray())
        } else {
            AudioFrame.fromData(null)
        }
    }
}