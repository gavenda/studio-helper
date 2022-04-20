package bogus.extension.music.command

import bogus.extension.music.*
import bogus.extension.music.checks.hasDJRole
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestIntMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

suspend fun MusicExtension.volume() {
    ephemeralSlashCommand(::VolumeArgs) {
        name = "volume"
        description = "volume.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            respond {
                player.volumeTo(arguments.volume)
                content = translate("volume.response", arrayOf(arguments.volume))
            }
        }
    }
}

internal class VolumeArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    private val volumeMap = buildMap {
        for (v in 0..100 step 10) {
            put(v.toString(), v)
        }
    }

    val volume by int {
        name = "volume"
        description = tp.translate(
            key = "volume.args.volume.description",
            bundleName = TRANSLATION_BUNDLE,
            replacements = arrayOf(VOLUME_MAX, VOLUME_MIN)
        )

        validate {
            if (value > VOLUME_MAX) {
                fail(translate("volume.validate.max.fail", TRANSLATION_BUNDLE, arrayOf(VOLUME_MAX)))
                return@validate
            }
            if (value < VOLUME_MIN) {
                fail(translate("volume.validate.min.fail", TRANSLATION_BUNDLE, arrayOf(VOLUME_MIN)))
                return@validate
            }

            pass()
        }

        autoComplete {
            suggestIntMap(volumeMap)
        }
    }
}