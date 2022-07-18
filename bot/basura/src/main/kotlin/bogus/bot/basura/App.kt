package bogus.bot.basura

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.about.AboutExtension
import bogus.extension.anilist.AniListExtension
import bogus.extension.listenmoe.AniRadioExtension
import bogus.lib.database.setupDatabase
import bogus.util.asFMTLogger
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
    testGuildId: Snowflake = Snowflake(envOrNull("TEST_GUILD_ID") ?: "0"),
): ExtensibleBot {
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD
    val log = KotlinLogging.logger { }.asFMTLogger()
    return ExtensibleBot(token) {
        AboutExtension.EMBED_COLOR = 0xFF0000
        AniListExtension.EMBED_COLOR = 0xFF0000
        AniRadioExtension.EMBED_COLOR = 0xFF0000

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

            created {
                setupDatabase()
            }

            setup {
                log.info {
                    message = "Bot started"
                    context = mapOf("bot" to "basura")
                }
            }
        }

        presence {
            competing("Trash")
        }
    }
}
