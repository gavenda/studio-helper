package bogus.bot.vivy

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.about.AboutExtension
import bogus.extension.music.MusicExtension
import bogus.util.asLogFMT
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
    testGuildId: Snowflake = Snowflake(env("TEST_GUILD_ID")),
): ExtensibleBot {
    val log = KotlinLogging.logger { }.asLogFMT()
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD

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
            setup {
                log.info("Bot started", mapOf("bot" to "vivy"))
            }
        }

        presence {
            playing("Fluorite Eye's Song")
        }
    }
}