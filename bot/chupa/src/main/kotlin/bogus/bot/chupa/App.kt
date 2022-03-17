package bogus.bot.chupa

import bogus.extension.counter.CounterExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val bot = ExtensibleBot(token) {
        extensions {
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
    }

    bot.start()
}
