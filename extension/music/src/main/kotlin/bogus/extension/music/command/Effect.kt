package bogus.extension.music.command

import bogus.extension.music.*
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.checks.hasDJRole
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashGroup
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.KoinComponent
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
            hasDJRole()
        }
        action {
            player.effects.applyNightcore(arguments.speed.coerceIn(10, 300))
            log.info { """msg="Applied filter" filter="Nightcore" user=${user.id}""" }
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
            hasDJRole()
        }
        action {
            player.effects.applyKaraoke()
            log.info { """msg="Applied filter" filter="Karaoke" user=${user.id}""" }
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
            hasDJRole()
        }
        action {
            player.effects.applyVaporwave()
            log.info { """msg="Applied filter" filter="Vaporwave" user=${user.id}""" }
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
            hasDJRole()
        }
        action {
            player.effects.applyEqualizer(MusicEffects.Equalizer.ROCK)
            log.info { """msg="Applied equalizer" equalizer="ROCK" user=${user.id}""" }
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
            hasDJRole()
        }
        action {
            player.effects.applyEqualizer(MusicEffects.Equalizer.POP)
            log.info { """msg="Applied equalizer" equalizer="POP" user=${user.id}""" }
            respond {
                content = translate("effect.equalizer.response.pop")
            }
        }
    }
}

private suspend fun SlashGroup.trebleBass() {
    ephemeralSubCommand {
        name = "treble-bass"
        description = "effect.equalizer.treble-bass.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            player.effects.applyEqualizer(MusicEffects.Equalizer.BASS_BOOST)
            log.info { """msg="Applied equalizer" equalizer="BASS_BOOST" user=${user.id}""" }
            respond {
                content = translate("effect.equalizer.response.treble-bass")
            }
        }
    }
}

internal class NightcoreArgs : KoinComponent, Arguments() {
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