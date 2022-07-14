package bogus.extension.aniradio

import bogus.extension.aniradio.command.disconnect
import bogus.extension.aniradio.command.playing
import bogus.extension.aniradio.command.radio
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import dev.kord.common.annotation.KordVoice
import dev.kord.gateway.Intent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@OptIn(KordVoice::class)
class AniRadioExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME

    companion object {
        var EMBED_COLOR = 0
    }

    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        registerSourceManager(HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY))
    }
    val webSocketPool = Executors.newSingleThreadExecutor()
    val webSocketScope = CoroutineScope(webSocketPool.asCoroutineDispatcher())
    val buffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }
    val player: AudioPlayer = playerManager.createPlayer()
    val log = KotlinLogging.logger { }.asFMTLogger()
    var voiceConnection: VoiceConnection? = null
    val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = AniRadioFrameConverter()
        }
    }
    var song = ListenSong.EMPTY

    override suspend fun setup() {
        // We need guild voice states
        intents += Intent.GuildVoiceStates

        radio()
        disconnect()
        playing()

        setupWebSocket()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun setupWebSocket() {
        log.info { message = "Setting up websockets" }

        webSocketScope.launch {
            var heartBeatMillis = 0L
            var lastHeartBeat = 0L
            val heartbeat = ListenFrame(op = ListenOp.HEARTBEAT)

            client.wss(ANIME_RADIO_GATEWAY) {
                while (isActive) {
                    if (incoming.isEmpty) {
                        val deltaMillis = System.currentTimeMillis() - lastHeartBeat

                        if (deltaMillis >= heartBeatMillis) {
                            lastHeartBeat = System.currentTimeMillis()

                            log.debug {
                                message = "Sending heartbeat"
                                context = mapOf(
                                    "frame" to heartbeat
                                )
                            }

                            sendSerialized(heartbeat)
                        }

                        delay(1000)
                        continue
                    }

                    val frame = receiveDeserialized<ListenFrame>()

                    log.info {
                        message = "Received op code"
                        context = mapOf(
                            "op" to frame.op
                        )
                    }

                    if (frame.op == ListenOp.HEARTBEAT) {
                        log.info {
                            message = "Received welcome message"
                            context = mapOf(
                                "message" to frame.data?.message
                            )
                        }

                        frame.data?.heartbeat?.let {
                            heartBeatMillis = it
                        }

                        lastHeartBeat = System.currentTimeMillis()
                    }

                    if (frame.op == ListenOp.PLAYBACK) {
                        log.info {
                            message = "Playback changed"
                            context = mapOf(
                                "music" to frame.data?.song?.title
                            )
                        }
                        frame.data?.song?.let {
                            song = it
                        }
                    }
                }
            }
        }
    }

    fun playAudio() {
        playerManager.loadItem(ANIME_RADIO_URI, object : AudioLoadResultHandler {
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