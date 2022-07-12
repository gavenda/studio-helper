package bogus.extension.music.command

import bogus.extension.music.*
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.player.EqualizerType
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject

suspend fun MusicExtension.effect() {
    ephemeralSlashCommand {
        name = "effect"
        description = "effect.description"

        group("filter") {
            description = "effect.filter.description"

            nightcore()
            karaoke()
            vaporwave()
        }

        group("equalizer") {
            description = "effect.equalizer.description"

            rock()
            pop()
            trebleBass()
        }
    }
}

private suspend fun SlashGroup.nightcore() {
    ephemeralSubCommand(::NightcoreArgs) {
        name = "nightcore"
        description = "effect.filter.nightcore.description"
        check {
            anyGuild()
        }
        action {
            val speed = arguments.speed.coerceIn(10, 300)

            player.effects.applyNightcore(speed)
            player.updateBoundQueue()

            log.info {
                message = "Applied filter"
                context = mapOf(
                    "filter" to "nightcore",
                    "user" to user.id,
                    "speed" to speed
                )
            }

            respond {
                content = translate("effect.filter.response.nightcore")
            }
        }
    }
}

private suspend fun SlashGroup.karaoke() {
    ephemeralSubCommand {
        name = "karaoke"
        description = "effect.filter.karaoke.description"
        check {
            anyGuild()
        }
        action {
            player.effects.applyKaraoke()
            player.updateBoundQueue()

            log.info {
                message = "Applied filter"
                context = mapOf(
                    "filter" to "karaoke",
                    "user" to user.id
                )
            }

            respond {
                content = translate("effect.filter.response.karaoke")
            }
        }
    }
}

private suspend fun SlashGroup.vaporwave() {
    ephemeralSubCommand {
        name = "vaporwave"
        description = "effect.filter.vaporwave.description"
        check {
            anyGuild()
        }
        action {
            player.effects.applyVaporwave()
            player.updateBoundQueue()

            log.info {
                message = "Applied filter"
                context = mapOf(
                    "filter" to "vaporwave",
                    "user" to user.id
                )
            }

            respond {
                content = translate("effect.filter.response.vaporwave")
            }
        }
    }
}

private suspend fun SlashGroup.rock() {
    ephemeralSubCommand {
        name = "rock"
        description = "effect.equalizer.rock.description"
        check {
            anyGuild()
        }
        action {
            player.effects.applyEqualizer(EqualizerType.ROCK)
            player.updateBoundQueue()

            log.info {
                message = "Applied equalizer"
                context = mapOf(
                    "filter" to EqualizerType.ROCK,
                    "user" to user.id
                )
            }

            respond {
                content = translate("effect.equalizer.response.rock")
            }
        }
    }
}

private suspend fun SlashGroup.pop() {
    ephemeralSubCommand {
        name = "pop"
        description = "effect.equalizer.pop.description"
        check {
            anyGuild()
        }
        action {
            player.effects.applyEqualizer(EqualizerType.POP)
            player.updateBoundQueue()

            log.info {
                message = "Applied equalizer"
                context = mapOf(
                    "filter" to EqualizerType.POP,
                    "user" to user.id
                )
            }

            respond {
                content = translate("effect.equalizer.response.pop")
            }
        }
    }
}

private suspend fun SlashGroup.trebleBass() {
    ephemeralSubCommand {
        name = "bass-boost"
        description = "effect.equalizer.bass-boost.description"
        check {
            anyGuild()
        }
        action {
            player.effects.applyEqualizer(EqualizerType.BASS_BOOST)
            player.updateBoundQueue()

            log.info {
                message = "Applied equalizer"
                context = mapOf(
                    "filter" to EqualizerType.BASS_BOOST,
                    "user" to user.id
                )
            }

            respond {
                content = translate("effect.equalizer.response.treble-bass")
            }
        }
    }
}

private class NightcoreArgs : KordExKoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    val speed by int {
        name = "speed"
        description = tp.translate(
            key = "effect.args.nightcore.description",
            bundleName = TRANSLATION_BUNDLE,
            replacements = arrayOf(NIGHTCORE_MAX, NIGHTCORE_MIN)
        )

        validate {
            if (value > NIGHTCORE_MAX) {
                fail(translate("nightcore.validate.max.fail", TRANSLATION_BUNDLE, arrayOf(NIGHTCORE_MAX)))
                return@validate
            }
            if (value < NIGHTCORE_MIN) {
                fail(translate("nightcore.validate.min.fail", TRANSLATION_BUNDLE, arrayOf(NIGHTCORE_MIN)))
                return@validate
            }

            pass()
        }
    }
}