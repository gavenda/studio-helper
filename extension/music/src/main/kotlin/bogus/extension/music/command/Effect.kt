package bogus.extension.music.command

import bogus.extension.music.MusicExtension
import bogus.extension.music.NIGHTCORE_MAX
import bogus.extension.music.NIGHTCORE_MIN
import bogus.extension.music.player
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
        allowInDms = false

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

            respond {
                content = translate("response.effect.filter.nightcore")
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

            respond {
                content = translate("response.effect.filter.karaoke")
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

            respond {
                content = translate("response.effect.filter.vaporwave")
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

            respond {
                content = translate("response.effect.equalizer.rock")
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

            respond {
                content = translate("response.effect.equalizer.pop")
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

            respond {
                content = translate("response.effect.equalizer.bass-boost")
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
                fail(
                    translate(
                        "command.effect.filter.nightcore.args.speed.validate.max.fail",
                        replacements = arrayOf(NIGHTCORE_MAX)
                    )
                )
                return@validate
            }
            if (value < NIGHTCORE_MIN) {
                fail(
                    translate(
                        "command.effect.filter.nightcore.args.speed.validate.min.fail",
                        replacements = arrayOf(NIGHTCORE_MIN)
                    )
                )
                return@validate
            }

            pass()
        }
    }
}