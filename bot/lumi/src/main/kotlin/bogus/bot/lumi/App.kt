package bogus.bot.lumi

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.about.AboutExtension
import bogus.extension.administration.AdministrationExtension
import bogus.extension.counter.CounterExtension
import bogus.extension.information.InformationExtension
import bogus.extension.moderation.ModerationExtension
import bogus.extension.utility.UtilityExtension
import bogus.lib.database.setupDatabase

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

const val BOT_EMBED_COLOR = 7912993

suspend fun main() {
    val token = env("TOKEN")
    lumi(token).start()
}

suspend fun lumi(
    token: String,
    testGuildId: Snowflake = Snowflake(envOrNull("TEST_GUILD_ID") ?: "0"),
): ExtensibleBot {
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD
    val log = KotlinLogging.logger { }

    return ExtensibleBot(token) {
        AboutExtension.EMBED_COLOR = BOT_EMBED_COLOR
        CounterExtension.EMBED_COLOR = BOT_EMBED_COLOR
        InformationExtension.EMBED_COLOR = BOT_EMBED_COLOR

        extensions {
            add(::AboutExtension)
            add(::AdministrationExtension)
            add(::CounterExtension)
            add(::InformationExtension)
            add(::ModerationExtension)
            add(::UtilityExtension)

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
                log.info { "Bot started" }
            }
        }

        presence {
            watching("Euphie")
        }
    }
}