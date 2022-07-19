package bogus.extension.music.command

import bogus.extension.music.*
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestIntMap

suspend fun MusicExtension.volume() {
    ephemeralSlashCommand(::VolumeArgs) {
        name = "command.volume"
        description = "command.volume.description"
        allowInDms = false

        check {
            anyGuild()
        }
        action {
            respond {
                player.volumeTo(arguments.volume)
                content = translate("response.volume", arrayOf(arguments.volume))
            }
        }
    }
}

private class VolumeArgs : KordExKoinComponent, Arguments() {
    private val volumeMap = buildMap {
        for (v in 0..100 step 10) {
            put(v.toString(), v)
        }
    }

    val volume by int {
        name = "command.volume.args.volume"
        description = "command.volume.args.volume.description"

        validate {
            if (value > VOLUME_MAX) {
                fail(translate("command.volume.args.volume.validate.max.fail", replacements = arrayOf(VOLUME_MAX)))
                return@validate
            }
            if (value < VOLUME_MIN) {
                fail(translate("command.volume.args.volume.validate.min.fail", replacements = arrayOf(VOLUME_MIN)))
                return@validate
            }

            pass()
        }

        autoComplete {
            suggestIntMap(volumeMap)
        }
    }
}