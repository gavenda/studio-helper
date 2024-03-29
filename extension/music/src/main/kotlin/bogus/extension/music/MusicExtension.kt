package bogus.extension.music

import bogus.extension.music.command.*
import bogus.extension.music.command.message.playLater
import bogus.extension.music.command.message.playNext
import bogus.extension.music.command.message.playNow
import bogus.lib.database.migrate

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import de.sonallux.spotify.api.SpotifyWebApi
import de.sonallux.spotify.api.authorization.SpotifyAuthorizationException
import de.sonallux.spotify.api.authorization.client_credentials.ClientCredentialsFlow
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.gateway.Intent
import dev.schlaubi.lavakord.kord.lavakord
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.concurrent.thread

object MusicExtension : Extension() {
    override val name: String = EXTENSION_NAME
    override val bundle: String = TRANSLATION_BUNDLE
    internal val log = KotlinLogging.logger { }

    var EMBED_COLOR = 0

    override suspend fun setup() {
        intents += Intent.GuildVoiceStates

        setupEvents()
        setupSlashCommands()
        setupMessageCommands()

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            runBlocking {
                Jukebox.destroy()
            }
        })
    }

    private suspend fun setupEvents() {
        event<ReadyEvent> {
            action {
                if (LAVAKORD_ENABLED) {
                    setupLavaKord()
                } else {
                    setupLavaPlayer()
                }

                setupSpotify()
                setupDatabase()
            }
        }
        event<GuildCreateEvent> {
            action {
                Jukebox.register(event.guild.id)
                Jukebox.bind(event.guild)
            }
        }
        event<Event> {
            action {
                log.debug { "Attempt disconnect" }
                Jukebox.tryToDisconnect()
            }
        }
    }

    private fun setupDatabase() {
        migrate(
            path = "classpath:db/music/migration",
            schema = EXTENSION_NAME
        )
    }

    private fun setupSpotify() {
        if (!SPOTIFY_ENABLED) return
        log.info { "Using Spotify Client" }
        loadModule {
            factory<SpotifyWebApi> {
                val credentials = ClientCredentialsFlow(
                    env("SPOTIFY_CLIENT_ID"),
                    env("SPOTIFY_CLIENT_SECRET")
                )

                try {
                    credentials.authorize()
                } catch (e: SpotifyAuthorizationException) {
                    log.error(e) { "Unable to authorize spotify" }
                }

                SpotifyWebApi.builder().authorization(credentials).build()
            }
        }
    }

    private fun setupLavaKord() {
        log.info { "Using Lava Link" }
        loadModule(createdAtStart = true) {
            single {
                val linkPasswords = env("LINK_PASSWORDS").split(";")
                val linkNodes = env("LINK_NODES").split(";")
                val lavaKord = kord.lavakord {
                    link {
                        autoReconnect = true
                    }
                }

                return@single lavaKord.apply {
                    linkNodes.forEachIndexed { index, node ->
                        addNode(node, linkPasswords[index])
                    }
                }
            }
        }
    }

    private fun setupLavaPlayer() {
        log.info { "Using Lava Player" }
        loadModule(createdAtStart = true) {
            single<AudioPlayerManager> {
                DefaultAudioPlayerManager().apply {
                    val email = envOrNull("YOUTUBE_EMAIL")
                    val password = envOrNull("YOUTUBE_PASSWORD")

                    // Local
                    registerSourceManager(LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY))

                    // Remote
                    registerSourceManager(YandexMusicAudioSourceManager(false))
                    registerSourceManager(YoutubeAudioSourceManager(true, email, password))
                    registerSourceManager(SoundCloudAudioSourceManager.createDefault())
                    registerSourceManager(BandcampAudioSourceManager())
                    registerSourceManager(VimeoAudioSourceManager())
                    registerSourceManager(TwitchStreamAudioSourceManager())
                    registerSourceManager(BeamAudioSourceManager())
                    registerSourceManager(GetyarnAudioSourceManager())
                    registerSourceManager(HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY))

                    configuration.isFilterHotSwapEnabled = true
                    configuration.frameBufferFactory = AudioFrameBufferFactory { bufferDuration, format, stopping ->
                        NonAllocatingAudioFrameBuffer(bufferDuration, format, stopping)
                    }
                }
            }
        }
    }

    private suspend fun setupMessageCommands() {
        playLater()
        playNext()
        playNow()
    }

    private suspend fun setupSlashCommands() {
        bind()
        unbind()
        clear()
        disconnect()
        effect()
        loop()
        pause()
        play()
        playlist()
        queue()
        remove()
        resume()
        shuffle()
        skip()
        stop()
        volume()
    }
}
