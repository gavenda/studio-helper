package bogus.extension.aniradio.command

import bogus.extension.aniradio.AniRadioExtension
import bogus.extension.aniradio.AniRadioExtension.Companion.EMBED_COLOR
import bogus.extension.aniradio.ListenSong
import bogus.extension.aniradio.RadioType
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingEnumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.optionalEnumChoice
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.builder.message.create.embed
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

suspend fun AniRadioExtension.playing() {
    ephemeralSlashCommand(::RadioTypeArgs) {
        name = "command.playing"
        description = "command.playing.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val radio = radioByGuild(guild.id)
            val song = songs[arguments.type ?: radio.radioType] ?: ListenSong.EMPTY
            respond {
                embed {
                    color = Color(EMBED_COLOR)
                    author {
                        name = "Current Playing Song"
                        icon = "https://github.com/LISTEN-moe.png"
                    }

                    title = song.title

                    if (song.artists.isNotEmpty()) {
                        field {
                            name = "Artist"
                            value = buildString {
                                song.artists.forEach {
                                    val nameRomaji = it.nameRomaji
                                    if (nameRomaji != null) {
                                        append("- [${it.name} ($nameRomaji)](https://listen.moe/artists/${it.id})")
                                    } else {
                                        append("- [${it.name}](https://listen.moe/artists/${it.id})")
                                    }
                                    append("\n")
                                }
                            }.dropLast(1)
                        }
                    }

                    if (song.albums.isNotEmpty()) {
                        song.albums.first().image?.let {
                            thumbnail {
                                url = "https://cdn.listen.moe/covers/$it"
                            }
                        }

                        field {
                            name = "Albums"
                            value = buildString {
                                song.albums.forEach {
                                    val nameRomaji = it.nameRomaji
                                    if (nameRomaji != null) {
                                        append("- [${it.name} ($nameRomaji)](https://listen.moe/albums/${it.id})")
                                    } else {
                                        append("- [${it.name}](https://listen.moe/albums/${it.id})")
                                    }
                                    append("\n")
                                }
                            }.dropLast(1)
                        }
                    }

                    if (song.duration > 0) {
                        field {
                            name = "Duration"
                            value = String.format(
                                "%02d:%02d",
                                song.duration.seconds.inWholeMinutes,
                                song.duration.seconds.inWholeSeconds % 1.minutes.inWholeSeconds
                            )
                        }
                    }

                    footer {
                        text = "Feel free to visit LISTEN.moe and listen there!"
                    }
                }
            }
        }
    }
}

private class RadioTypeArgs : Arguments() {
    val type by optionalEnumChoice<RadioType> {
        name = "command.radio.args.type"
        description = "command.radio.args.type.description"
        typeName = "RadioType"
    }
}