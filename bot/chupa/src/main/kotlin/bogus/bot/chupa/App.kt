package bogus.bot.chupa

import bogus.extension.about.AboutExtension
import bogus.extension.counter.CounterExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val log = KotlinLogging.logger {  }
    val bot = ExtensibleBot(token) {
        extensions {
            add(::AboutExtension)
            add(::CounterExtension)

            help {
                enableBundledExtension = false
            }
        }

        applicationCommands {
            if (environment == "dev") {
                defaultGuild = Snowflake(env("TEST_GUILD_ID"))
            }
        }

        hooks {
            setup {
                log.info { """msg="Bot up and running"""" }
            }
        }

        presence {
            playing("Heavenly Cock")
        }
    }

    bot.start()
}
