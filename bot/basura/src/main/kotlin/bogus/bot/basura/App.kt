package bogus.bot.basura

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
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
    val token = env("TOKEN")
    basura(token).start()
}

suspend fun basura(
    token: String,
    testGuildId: Snowflake = Snowflake(env("TEST_GUILD_ID"))
): ExtensibleBot {
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD
    val log = KotlinLogging.logger { }.asLogFMT()
    return ExtensibleBot(token) {
        extensions {
            add(::AboutExtension)
            add { AniListExtension }
            add(::AniRadioExtension)

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
                log.info("Bot started", mapOf("bot" to "basura"))
            }
        }

        presence {
            competing("Trash")
        }
    }
}
