package bogus.extension.aniradio.command

import bogus.extension.aniradio.AniRadioExtension
import bogus.extension.aniradio.AniRadioExtension.Companion.EMBED_COLOR
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.builder.message.create.embed
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

suspend fun AniRadioExtension.playing() {
    ephemeralSlashCommand {
        name = "command.playing"
        description = "command.playing.description"
        check {
            anyGuild()
        }
        action {
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
                                        append("[${it.name} ($nameRomaji)](https://listen.moe/artists/${it.id})")
                                    } else {
                                        append("[${it.name}](https://listen.moe/artists/${it.id})")
                                    }
                                    append(", ")
                                }
                            }.dropLast(2)
                            inline = true
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
                                        append("[${it.name} ($nameRomaji)](https://listen.moe/albums/${it.id})")
                                    } else {
                                        append("[${it.name}](https://listen.moe/albums/${it.id})")
                                    }
                                    append(", ")
                                }
                            }.dropLast(2)
                            inline = true
                        }
                    }

                    field {
                        name = "Duration"
                        value = String.format(
                            "%02d:%02d",
                            song.duration.seconds.inWholeMinutes,
                            song.duration.seconds.inWholeSeconds % 1.minutes.inWholeSeconds
                        )
                        inline = true
                    }

                    footer {
                        text = "Feel free to visit LISTEN.moe and listen there!"
                    }
                }
            }
        }
    }
}