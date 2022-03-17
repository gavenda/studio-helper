package bogus.bot.vivy

import bogus.extension.music.MusicExtension
import bogus.extension.about.AboutExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val bot = ExtensibleBot(token) {
        extensions {
            add { MusicExtension }
            add(::SingExtension)
            add(::AboutExtension)
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
            kordShutdownHook = true
        }

        presence {
            playing("Fluorite Eye's Song")
        }
    }

    bot.start()
}
