package bogus.bot.basura

import bogus.extension.about.AboutExtension
import bogus.extension.anilist.AniListExtension
import bogus.extension.aniradio.AniRadioExtension
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
            add { AniListExtension }
            add(::AniRadioExtension)

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
            competing("Trash")
        }
    }

    bot.start()
}
