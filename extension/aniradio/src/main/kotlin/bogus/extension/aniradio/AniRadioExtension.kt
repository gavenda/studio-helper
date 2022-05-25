package bogus.extension.aniradio

import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermissions
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
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.connect
import dev.kord.gateway.Intent
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import io.ktor.util.*
import mu.KotlinLogging
import java.nio.ByteBuffer

@OptIn(KordVoice::class)
class AniRadioExtension : Extension() {
    override val name = EXTENSION_NAME
    override val bundle = TRANSLATIONS_BUNDLE

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager().apply {
        registerSourceManager(HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY))
    }
    private val buffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE)
    private val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }
    private val player: AudioPlayer = playerManager.createPlayer()
    private val log = KotlinLogging.logger { }.asLogFMT()
    private var voiceConnection: VoiceConnection? = null

    override suspend fun setup() {
        // We need guild voice states
        intents += Intent.GuildVoiceStates

        ephemeralSlashCommand {
            name = "radio"
            description = "radio.description"
            check {
                anyGuild()
            }
            action {
                val guild = guild ?: return@action
                val selfMember = guild.getMemberOrNull(event.kord.selfId)
                val member = memberFor(event)

                if (selfMember == null) {
                    respond {
                        content = translate("checks.voiceChannel.error")
                    }
                    return@action
                }
                if (member == null) {
                    respond {
                        content = translate("checks.voiceChannel.error")
                    }
                    return@action
                }

                val ourVoiceChannel = selfMember.getVoiceStateOrNull()?.getChannelOrNull()
                val theirVoiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()

                if (theirVoiceChannel == null) {
                    log.debug("No voice channel")
                    respond {
                        content = translate("checks.voiceChannel.notInVoice")
                    }
                    return@action
                }

                if (theirVoiceChannel != ourVoiceChannel) {
                    val canTalk = selfMember.hasPermissions(
                        Permission.Speak,
                        Permission.Connect
                    )
                    if (!canTalk) {
                        log.debug("No permission")
                        respond {
                            content = translate("checks.voiceChannel.noPermission")
                        }
                        return@action
                    }

                    // Connect
                    voiceConnection = theirVoiceChannel.connect {
                        audioProvider { audioProvider() }
                    }
                }

                respond {
                    content = translate("radio.response")
                }

                playAudio()
            }
        }

        ephemeralSlashCommand {
            name = "disconnect"
            description = "disconnect.description"
            check {
                anyGuild()
            }
            action {
                voiceConnection?.apply {
                    leave()
                    voiceConnection = null
                    respond {
                        content = translate("disconnect.response")
                    }
                } ?: respond {
                    content = translate("disconnect.notConnected")
                }
            }
        }
    }

    private fun playAudio() {
        playerManager.loadItem(ANIME_RADIO_URI, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = player.playTrack(track)
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
            AudioFrame.fromData(buffer.flip().moveToByteArray())
        } else {
            AudioFrame.fromData(null)
        }
    }
}