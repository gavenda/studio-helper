package bogus.extension.music

import bogus.extension.music.command.*
import bogus.extension.music.command.message.playLater
import bogus.extension.music.command.message.playNext
import bogus.extension.music.command.message.playNow
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import org.flywaydb.core.Flyway
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import javax.sql.DataSource
import kotlin.concurrent.thread

object MusicExtension : Extension() {
    override val name: String = EXTENSION_NAME
    override val bundle: String = TRANSLATION_BUNDLE
    internal val log = KotlinLogging.logger { }.asLogFMT()

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
                log.debug("Attempt disconnect")
                Jukebox.tryToDisconnect()
            }
        }
    }

    private fun setupDatabase() {
        loadModule {
            single<DataSource>(createdAtStart = true) {
                HikariDataSource(HikariConfig().apply {
                    maximumPoolSize = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                    jdbcUrl = env("MUSIC_DB_URL")
                    username = env("MUSIC_DB_USER")
                    password = env("MUSIC_DB_PASS")
                })
            }
            single {
                Database.connect(
                    dataSource = get(),
                    dialect = PostgreSqlDialect()
                )
            }
        }

        val hikari by inject<DataSource>()

        Flyway.configure()
            .dataSource(hikari)
            .load()
            .migrate()
    }

    private fun setupSpotify() {
        if (!SPOTIFY_ENABLED) return
        log.info("Using Spotify Client")
        loadModule {
            factory<SpotifyWebApi> {
                val credentials = ClientCredentialsFlow(
                    env("SPOTIFY_CLIENT_ID"),
                    env("SPOTIFY_CLIENT_SECRET")
                )

                try {
                    credentials.authorize()
                } catch (e: SpotifyAuthorizationException) {
                    log.error("""msg="Unable to authorize spotify"""")
                }

                SpotifyWebApi.builder().authorization(credentials).build()
            }
        }
    }

    private fun setupLavaKord() {
        log.info("Using Lava Link")
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
        log.info("Using Lava Player")
        loadModule(createdAtStart = true) {
            single<AudioPlayerManager> {
                DefaultAudioPlayerManager().apply {
                    AudioSourceManagers.registerRemoteSources(this)
                    AudioSourceManagers.registerLocalSource(this)

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
