package bogus.bot.parrot

import bogus.extension.announcer.AnnouncerExtension
import bogus.extension.automove.AutoMoveExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val environment = envOrNull("ENVIRONMENT") ?: "production"
    val token = env("TOKEN")
    val log = KotlinLogging.logger { }
    val bot = ExtensibleBot(token) {
        extensions {
            val defaultGuildId = Snowflake(env("DEFAULT_GUILD_ID"))
            val defaultVoiceChannelId = Snowflake(env("DEFAULT_VOICE_CHANNEL_ID"))
            val deafVoiceChannelId = Snowflake(env("DEAF_VOICE_CHANNEL_ID"))

            add {
                AnnouncerExtension(
                    defaultGuildId = defaultGuildId,
                    defaultVoiceChannelId = defaultVoiceChannelId,
                    audioFiles = AUDIO_FILES
                )
            }
            add {
                AutoMoveExtension(
                    defaultChannel = defaultVoiceChannelId,
                    deafChannel = deafVoiceChannelId
                )
            }

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
            listening("Birds")
        }
    }

    bot.start()
}
