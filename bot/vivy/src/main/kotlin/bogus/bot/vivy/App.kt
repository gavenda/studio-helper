package bogus.bot.vivy

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.about.AboutExtension
import bogus.extension.music.MusicExtension
import bogus.lib.database.setupDatabase
import bogus.lib.metrics.setupMetrics

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val token = env("TOKEN")
    vivy(token).start()
}

suspend fun vivy(
    token: String,
    testGuildId: Snowflake = Snowflake(envOrNull("TEST_GUILD_ID") ?: "0"),
): ExtensibleBot {
    val log = KotlinLogging.logger { }
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD

    AboutExtension.EMBED_COLOR = 0x00FFFF
    MusicExtension.EMBED_COLOR = 0x00FFFF

    return ExtensibleBot(token) {
        extensions {
            add { MusicExtension }
            add(::SingExtension)
            add(::AboutExtension)
            help {
                enableBundledExtension = false
            }
        }

        applicationCommands {
            if (environment == ENVIRONMENT_DEV) {
                defaultGuild = testGuildId
            }
        }

        hooks {
            kordShutdownHook = true

            created {
                setupMetrics()
                setupDatabase()
            }

            setup {
                log.info { "Bot started" }
            }
        }

        presence {
            playing("Fluorite Eye's Song")
        }
    }
}