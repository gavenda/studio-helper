package bogus.bot.parrot

import bogus.constants.ENVIRONMENT_DEV
import bogus.constants.ENVIRONMENT_PROD
import bogus.extension.announcer.AnnouncerExtension
import bogus.extension.automove.AutoMoveExtension
import bogus.util.asFMTLogger
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import mu.KotlinLogging

suspend fun main() {
    val token = env("TOKEN")
    parrot(token).start()
}

suspend fun parrot(
    token: String,
    testGuildId: Snowflake = Snowflake(envOrNull("TEST_GUILD_ID") ?: "0"),
    defaultGuildId: Snowflake = Snowflake(env("DEFAULT_GUILD_ID")),
    defaultVoiceChannelId: Snowflake = Snowflake(env("DEFAULT_VOICE_CHANNEL_ID")),
    deafVoiceChannelId: Snowflake = Snowflake(env("DEAF_VOICE_CHANNEL_ID")),
    audioFileMapPath: String = envOrNull("AUDIO_FILE_MAP") ?: "config.json"
): ExtensibleBot {
    val environment = envOrNull("ENVIRONMENT") ?: ENVIRONMENT_PROD
    val log = KotlinLogging.logger { }.asFMTLogger()

    return ExtensibleBot(token) {
        extensions {
            add {
                AnnouncerExtension(
                    defaultGuildId = defaultGuildId,
                    defaultVoiceChannelId = defaultVoiceChannelId,
                    configPath = audioFileMapPath
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
            if (environment == ENVIRONMENT_DEV) {
                defaultGuild = testGuildId
            }
        }

        hooks {
            kordShutdownHook = true
            setup {
                log.info {
                    message = "Bot started"
                    context = mapOf("bot" to "parrot")
                }
            }
        }

        presence {
            listening("Birds")
        }
    }
}