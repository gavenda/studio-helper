package bogus.extension.announcer


import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.FilterStrategy
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.gateway.Intent
import dev.kord.voice.AudioFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random

@OptIn(KordVoice::class)
class AnnouncerExtension(
    val defaultGuildId: Snowflake,
    val defaultVoiceChannelId: Snowflake,
    val audioFiles: Map<String, String>
) : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = TRANSLATIONS_BUNDLE

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        AudioSourceManagers.registerLocalSource(this)
    }
    private val buffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    private val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }
    private val player: AudioPlayer = playerManager.createPlayer()
    private val log = KotlinLogging.logger { }.asLogFMT()
    private val filePaths = mutableMapOf<String, Path>()

    override suspend fun setup() {
        // We need guild voice states
        intents += Intent.GuildVoiceStates

        ephemeralSlashCommand {
            name = "join"
            description = "join.description"
            check {
                anyGuild()
                inVoiceChannel(audioProvider)
            }
            action {
                respond {
                    content = translate("join.response")
                }
            }
        }

        ephemeralSlashCommand(::AnnounceArgs) {
            name = "announce"
            description = "announce.description"
            check {
                anyGuild()
                inVoiceChannel(audioProvider)
            }
            action {
                respond {
                    content = translate("announce.response")
                }
                filePaths[arguments.name]?.let { playAudio(it) }
            }
        }

        event<VoiceStateUpdateEvent> {
            action {
                val user = kord.getUser(event.state.userId) ?: return@action
                if (user.isBot) return@action
                if (event.state.channelId != defaultVoiceChannelId) return@action
                if (event.state.channelId == event.old?.channelId) return@action
                if (event.state.channelId != null) {
                    val rndIdx = Random.nextInt(0, filePaths.size)
                    val boldFilePathList = filePaths.values.toList()
                    playAudio(boldFilePathList[rndIdx], 2000)
                }
            }
        }

        event<ReadyEvent> {
            action {
                withContext(Dispatchers.IO) {
                    audioFiles.forEach { (audioName, audioFile) ->
                        val (audioFileName, audioFileExt) = audioFile.split(".")
                        val audioFileStream =
                            object {}.javaClass.getResourceAsStream("/${audioFile}") ?: error("Cannot extract file")

                        filePaths[audioName] = Files.createTempFile(audioFileName, ".$audioFileExt").apply {
                            Files.write(this, audioFileStream.readAllBytes())
                            log.info(
                                msg = "Audio file extracted",
                                context = mapOf(
                                    "tmpDir" to this
                                )
                            )
                        }
                    }
                }

                // Auto join channel
                val guild = kord.getGuild(defaultGuildId) ?: return@action
                val voiceChannel = guild.getChannelOfOrNull<VoiceChannel>(defaultVoiceChannelId) ?: return@action

                voiceChannel.connect {
                    audioProvider { audioProvider() }
                }
            }
        }
    }

    private fun playAudio(path: Path, delay: Long = 0) {
        val filePathStr = path.toAbsolutePath().toString()
        playerManager.loadItem(filePathStr, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = runBlocking {
                delay(delay)
                player.playTrack(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {}
            override fun noMatches() {}
            override fun loadFailed(exception: FriendlyException) {
                log.error(
                    msg = "Audio file failed to load",
                    context = mapOf(
                        "reason" to exception.message
                    )
                )
            }
        })
    }

    private val audioProvider: () -> AudioFrame? = {
        val canProvide = player.provide(frame)
        if (canProvide) {
            AudioFrame.fromData(frame.data)
        } else {
            AudioFrame.fromData(null)
        }
    }

    inner class AnnounceArgs : KoinComponent, Arguments() {
        val tp by inject<TranslationsProvider>()
        val name by string {
            name = "name"
            description = tp.translate("announce.args.description", TRANSLATIONS_BUNDLE)

            autoComplete {
                val fileMap = mapOf(*filePaths.keys.map { it to it }.toTypedArray())
                suggestStringMap(fileMap, FilterStrategy.Contains)
            }
        }
    }
}