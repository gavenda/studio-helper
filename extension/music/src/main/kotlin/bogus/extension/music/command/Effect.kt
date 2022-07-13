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
import com.kotlindiscord.kord.extensions.types.respond

suspend fun MusicExtension.effect() {
    ephemeralSlashCommand {
        name = "command.effect"
        description = "command.effect.description"

        group("command.effect.filter") {
            description = "command.effect.filter.description"

            nightcore()
            karaoke()
            vaporwave()
        }

        group("command.effect.equalizer") {
            description = "command.effect.equalizer.description"

            rock()
            pop()
            trebleBass()
        }
    }
}

private suspend fun SlashGroup.nightcore() {
    ephemeralSubCommand(::NightcoreArgs) {
        name = "command.effect.filter.nightcore"
        description = "command.effect.filter.nightcore.description"
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
        name = "command.effect.filter.karaoke"
        description = "command.effect.filter.karaoke.description"
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
        name = "command.effect.filter.vaporwave"
        description = "command.effect.filter.vaporwave.description"
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
        name = "command.effect.equalizer.rock"
        description = "command.effect.equalizer.rock.description"
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
        name = "command.effect.equalizer.pop"
        description = "command.effect.equalizer.pop.description"
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
        name = "command.effect.equalizer.bass-boost"
        description = "command.effect.equalizer.bass-boost.description"
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
                content = translate("effect.equalizer.response.bass-boost")
            }
        }
    }
}

private class NightcoreArgs : Arguments() {
    val speed by int {
        name = "command.effect.filter.nightcore.args.speed"
        description = "command.effect.filter.nightcore.args.speed.description"

        validate {
            if (value > NIGHTCORE_MAX) {
                fail(translate("command.effect.filter.nightcore.args.speed.validate.max.fail", replacements = arrayOf(NIGHTCORE_MAX)))
                return@validate
            }
            if (value < NIGHTCORE_MIN) {
                fail(translate("command.effect.filter.nightcore.args.speed.validate.min.fail", replacements = arrayOf(NIGHTCORE_MIN)))
                return@validate
            }

            pass()
        }
    }
}