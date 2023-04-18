package bogus.extension.listenmoe

import bogus.extension.listenmoe.command.disconnect
import bogus.extension.listenmoe.command.playing
import bogus.extension.listenmoe.command.radio
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

class AniRadioExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = EXTENSION_NAME

    companion object {
        var EMBED_COLOR = 0
    }

    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        registerSourceManager(HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY))
    }
    val scheduler = Scheduler()
    val webSocketPool: ExecutorService = Executors.newSingleThreadExecutor()
    val webSocketScope = CoroutineScope(webSocketPool.asCoroutineDispatcher())
    val log = KotlinLogging.logger { }
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

    suspend fun setupJpopGateway() {
        webSocketScope.launch {
            client.wss(JPOP_RADIO_GATEWAY) {
                try {
                    while (isActive) {
                        receivePlayback {
                            songs[RadioType.JPOP] = it
                        }
                    }
                } catch (ex: Throwable) {
                    setupKpopGateway()
                    val closeReason = closeReason.await()
                    log.error(ex) { "Connection closed: $closeReason" }
                }
            }
        }
    }

    suspend fun setupKpopGateway() {
        webSocketScope.launch {
            client.wss(KPOP_RADIO_GATEWAY) {
                try {
                    while (isActive) {
                        receivePlayback {
                            songs[RadioType.KPOP] = it
                        }
                    }
                } catch (ex: Throwable) {
                    setupKpopGateway()
                    val closeReason = closeReason.await()
                    log.error(ex) { "Connection closed: $closeReason" }
                }
            }
        }
    }

    suspend fun setupWebSocket() {
        log.info { "Setting up websockets" }

        setupJpopGateway()
        setupKpopGateway()
    }

    suspend fun DefaultClientWebSocketSession.receivePlayback(playback: (ListenSong) -> Unit) {
        val frame = receiveDeserialized<ListenFrame>()

        log.debug { "Received op code [ op = ${frame.op} ]" }

        if (frame.op == ListenOp.WELCOME) {
            log.info { "Received welcome message" }

            if (frame.data?.heartbeat != null) {
                val duration = frame.data.heartbeat.milliseconds

                scheduler.schedule(duration, repeat = true) {
                    log.debug { "Sent heartbeat" }
                    sendSerialized(ListenFrame(op = ListenOp.HEARTBEAT))
                }
            } else {
                log.warn { "Heartbeat data is null" }
            }
        }

        if (frame.op == ListenOp.PLAYBACK) {
            log.debug { "Playback changed" }
            frame.data?.song?.let {
                playback(it)
            }
        }
    }

    fun radioByGuild(snowflake: Snowflake): RadioPlayer {
        return radios.computeIfAbsent(snowflake) {
            RadioPlayer(playerManager)
        }
    }
}