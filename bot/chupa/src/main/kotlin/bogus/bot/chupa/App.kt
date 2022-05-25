package bogus.bot.chupa

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.about.AboutExtension
import bogus.extension.counter.CounterExtension
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val token = env("TOKEN")
    chupa(token).start()
}

suspend fun chupa(
    token: String,
    testGuildId: Snowflake = Snowflake(envOrNull("TEST_GUILD_ID") ?: "0"),
): ExtensibleBot {
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD
    val log = KotlinLogging.logger { }.asLogFMT()

    return ExtensibleBot(token) {
        extensions {
            add(::AboutExtension)
            add(::CounterExtension)

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
                log.info("Bot started", mapOf("bot" to "chupa"))
            }
        }

        presence {
            playing("Heavenly Cock")
        }
    }
}
