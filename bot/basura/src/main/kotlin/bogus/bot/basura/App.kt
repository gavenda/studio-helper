package bogus.bot.basura

import bogus.extension.about.AboutExtension
import bogus.extension.anilist.AniListExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val bot = ExtensibleBot(token) {
        extensions {
            add(::AboutExtension)
            add { AniListExtension }

            help {
                enableBundledExtension = false
            }
        }

        applicationCommands {
            if (environment == "dev") {
                defaultGuild = Snowflake(env("TEST_GUILD_ID"))
            }
        }

        presence {
            competing("Trash")
        }
    }

    bot.start()
}
