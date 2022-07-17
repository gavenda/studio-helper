package bogus.extension.listenmoe

import bogus.extension.listenmoe.command.disconnect
import bogus.extension.listenmoe.command.playing
import bogus.extension.listenmoe.command.radio
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
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
    val log = KotlinLogging.logger { }.asFMTLogger()
    val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = AniRadioFrameConverter()
        }
    }
    val radios = ConcurrentHashMap<Snowflake, RadioPlayer>()
    val songs = ConcurrentHashMap<RadioType, ListenSong>()

    override suspend fun setup() {
        // We need guild voice states
        intents += Intent.GuildVoiceStates

        radio()
        disconnect()
        playing()

        setupWebSocket()
    }

    suspend fun setupWebSocket() {
        log.info { message = "Setting up websockets" }

        webSocketScope.launch {
            client.wss(JPOP_RADIO_GATEWAY) {
                receivePlayback { songs[RadioType.JPOP] = it }
            }
        }

        webSocketScope.launch {
            client.wss(KPOP_RADIO_GATEWAY) {
                receivePlayback { songs[RadioType.KPOP] = it }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun DefaultClientWebSocketSession.receivePlayback(playback: (ListenSong) -> Unit) {
        var heartBeatMillis = 0L
        var lastHeartBeat = 0L
        val heartbeat = ListenFrame(op = ListenOp.HEARTBEAT)

        while (isActive) {
            try {
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

                log.debug {
                    message = "Received op code"
                    context = mapOf(
                        "op" to frame.op,
                    )
                }

                if (frame.op == ListenOp.WELCOME) {
                    log.info {
                        message = "Received welcome message"
                        context = mapOf(
                            "message" to frame.data?.message,
                            "heartbeat" to frame.data?.heartbeat
                        )
                    }

                    frame.data?.heartbeat?.let {
                        heartBeatMillis = it
                    }

                    lastHeartBeat = System.currentTimeMillis()
                }

                if (frame.op == ListenOp.PLAYBACK) {
                    log.debug {
                        message = "Playback changed"
                        context = mapOf(
                            "music" to frame.data?.song?.title
                        )
                    }
                    frame.data?.song?.let {
                        playback(it)
                    }
                }
            } catch (ex: Exception) {
                log.error(ex) {
                    message = "An error occured during WebSocket handling, retrying..."
                }
            }
        }
    }

    fun radioByGuild(snowflake: Snowflake): RadioPlayer {
        return radios.computeIfAbsent(snowflake) {
            RadioPlayer(playerManager)
        }
    }
}