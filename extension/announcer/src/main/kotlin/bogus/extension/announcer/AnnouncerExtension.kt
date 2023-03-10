package bogus.extension.announcer

import bogus.collection.RollingList

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
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
import dev.inmo.krontab.doInfinityTz
import dev.kord.common.Color
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.gateway.Intent
import dev.kord.rest.builder.message.create.embed
import dev.kord.voice.AudioFrame
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(KordVoice::class)
class AnnouncerExtension(
    val defaultGuildId: Snowflake,
    val defaultVoiceChannelId: Snowflake,
    val configPath: String = "config.json",
) : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = TRANSLATIONS_BUNDLE

    companion object {
        var EMBED_COLOR = 0
    }

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        AudioSourceManagers.registerLocalSource(this)
    }
    private val buffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    private val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }
    private val player: AudioPlayer = playerManager.createPlayer()
    private val log = KotlinLogging.logger { }
    private val announceList = RollingList<AnnounceLog>()
    private val kronJobs = mutableListOf<Job>()
    private val kronContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override suspend fun setup() {
        // We need guild voice states
        intents += Intent.GuildVoiceStates

        ephemeralSlashCommand {
            name = "command.join"
            description = "command.join.description"
            check {
                anyGuild()
                inVoiceChannel(audioProvider)
            }
            action {
                respond {
                    content = translate("response.join")
                }
            }
        }

        ephemeralSlashCommand {
            name = "command.refresh"
            description = "command.refresh.description"
            check {
                anyGuild()
            }
            action {
                setupKron()
                respond {
                    content = "response.refresh"
                }
            }
        }

        ephemeralSlashCommand(::AnnounceArgs) {
            name = "command.announce"
            description = "command.announce.description"
            check {
                anyGuild()
                inVoiceChannel(audioProvider)
            }
            action {
                respond {
                    content = translate("response.announce")
                }
                filePaths[arguments.name]?.let {
                    announceList.add(AnnounceLog(user.mention, arguments.name, Instant.now()))
                    playAudio(it)
                }
            }
        }

        publicSlashCommand {
            name = "command.announce-logs"
            description = "command.announce-logs.description"
            check {
                anyGuild()
            }
            action {
                respond {
                    embed {
                        color = Color(EMBED_COLOR)
                        title = translate("response.announce-logs.title")
                        description = translate("response.announce-logs.description")

                        announceList.forEach { log ->
                            val dateFormatted = DATE_FORMATTER.format(log.timestamp)
                            field {
                                name = log.announced
                                value = translate(
                                    "response.announce-logs.field",
                                    replacements = arrayOf(dateFormatted, log.mention)
                                )
                            }
                        }
                        footer {
                            text = translate("response.announce-logs.footer.text")
                        }
                    }
                }
            }
        }

        event<VoiceStateUpdateEvent> {
            action {
                val user = kord.getUser(event.state.userId) ?: return@action
                if (user.isBot) return@action
                if (event.state.channelId != defaultVoiceChannelId) return@action
                if (event.state.channelId == event.old?.channelId) return@action
                if (event.state.channelId != null) {
                    // look for user specific map, then random
                    if (userFilePaths.containsKey(event.state.userId)) {
                        val userFilePathList = userFilePaths.getValue(event.state.userId)
                        val rndIdx = Random.nextInt(0, userFilePathList.size)
                        playAudio(userFilePathList[rndIdx], delayMillis)
                    } else {
                        val filePathList = filePaths.values.toList()
                        val rndIdx = Random.nextInt(0, filePaths.size)
                        playAudio(filePathList[rndIdx], delayMillis)
                    }
                }
            }
        }

        event<ReadyEvent> {
            action {
                // Auto join channel
                val guild = kord.getGuildOrNull(defaultGuildId) ?: return@action
                val voiceChannel = guild.getChannelOfOrNull<VoiceChannel>(defaultVoiceChannelId) ?: return@action

                voiceChannel.connect {
                    audioProvider { audioProvider() }
                }
                setupKron()
            }
        }
    }

    private val config: AnnouncerConfig
        get() {
            val cwd = Path.of("")
            val configStr = Files.readString(cwd.resolve(configPath), Charsets.UTF_8)
            return Json.decodeFromString(configStr)
        }

    private val delayMillis: Duration
        get() {
            return config.delay.milliseconds
        }

    private val userFilePaths: Map<Snowflake, List<Path>>
        get() {
            return config.userMapping.map { entry ->
                Snowflake(entry.key) to entry.value.map { Path.of("").resolve(it) }
            }.toMap()
        }

    private val filePaths: Map<String, Path>
        get() {
            return config.fileMapping.associate {
                it.name to Path.of("").resolve(it.filePath)
            }
        }

    private fun setupKron() {
        kronJobs.forEach { it.cancel() }

        config.kronMapping.forEach {
            val path = Path.of("").resolve(it.filePath)
            val job = CoroutineScope(kronContext).launch {
                doInfinityTz(it.kron) {
                    playAudio(path, delayMillis)
                }
            }
            kronJobs.add(job)
        }
    }

    private fun playAudio(path: Path, delay: Duration = 0.milliseconds) {
        val filePathStr = path.toAbsolutePath().toString()
        playerManager.loadItem(filePathStr, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = runBlocking {
                delay(delay)
                player.playTrack(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {}
            override fun noMatches() {}
            override fun loadFailed(ex: FriendlyException) {
                log.error(ex) { "Audio file failed to load: ${ex.message}" }
            }
        })
    }

    private val audioProvider: () -> AudioFrame? = {
        val canProvide = player.provide(frame)
        if (canProvide) {
            AudioFrame.fromData(buffer.flip().moveToByteArray())
        } else {
            AudioFrame.fromData(null)
        }
    }

    inner class AnnounceArgs : Arguments() {
        val name by string {
            name = "command.announce.args.name"
            description = "command.announce.args.name.description"

            autoComplete {
                val fileMap = mapOf(*filePaths.keys.map { it to it }.toTypedArray())
                suggestStringMap(fileMap, FilterStrategy.Contains)
            }
        }
    }
}