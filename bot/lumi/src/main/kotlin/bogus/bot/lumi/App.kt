package bogus.bot.lumi

import bogus.extension.about.AboutExtension
import bogus.extension.moderation.ModerationExtension
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val log = KotlinLogging.logger { }.asLogFMT()
    val bot = ExtensibleBot(token) {
        extensions {
            add(::AboutExtension)
            add(::ModerationExtension)

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
                log.info("Bot up and running")
            }
        }

        presence {
            watching("Amnesia")
        }
    }

    bot.start()
}
