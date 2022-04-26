package bogus.extension.music.command

import bogus.constants.ITEMS_PER_CHUNK
import bogus.extension.music.*
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.checks.hasDJRole
import bogus.extension.music.checks.inVoiceChannel
import bogus.extension.music.db.*
import bogus.paginator.editingStandardPaginator
import bogus.util.abbreviate
import bogus.util.escapedBrackets
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.support.postgresql.ilike

suspend fun MusicExtension.playlist() {
    ephemeralSlashCommand {
        name = "playlist"
        description = "playlist.description"

        list()
        show()
        create()
        delete()
        add()
        remove()
        queue()
    }
}

private suspend fun EphemeralSlashCommand<*>.list() {
    val db by inject<Database>()

    ephemeralSubCommand {
        name = "list"
        description = "playlist.list.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            var songList = buildString {
                db.from(DbPlaylists)
                    .leftJoin(DbPlaylistSongs, on = DbPlaylists.playlistId eq DbPlaylistSongs.playlistId)
                    .select(DbPlaylists.name, count(DbPlaylistSongs.playlistId))
                    .where { DbPlaylists.discordUserId eq user.idLong }
                    .groupBy(DbPlaylists.name)
                    .forEachIndexed { index, row ->
                        val number = index + 1
                        val name = row.getString(1)
                        val noOfSongs = row.getInt(2)
                        append("`$number.` $name - `$noOfSongs` songs\n")
                    }
            }

            if (songList.isBlank()) {
                songList = translate("playlist.list.empty")
            }

            respond {
                embed {
                    title = translate("playlist.list.title")
                    description = songList
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.show() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "show"
        description = "playlist.show.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                val dbPlaylistSongs = db.playlistSongs.filter {
                    (it.playlistId eq dbPlaylist.playlistId)
                }
                val resultsChunked = dbPlaylistSongs
                    .asKotlinSequence()
                    .chunked(ITEMS_PER_CHUNK)
                val paginator = editingStandardPaginator {
                    keepEmbed = true
                    resultsChunked.forEach { results ->
                        val songList = buildString {
                            results.forEach { row ->
                                val titleCleaned = row.title
                                    .abbreviate(EmbedBuilder.Limits.title)
                                    .escapedBrackets

                                append("`${row.playlistSongId}` [${titleCleaned}](${row.uri})\n")
                            }
                        }

                        page {
                            title = dbPlaylist.name
                            description = songList
                        }
                    }
                }

                paginator.send()
            } else {
                respond {
                    content = translate(
                        key = "playlist.response.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.create() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "create"
        description = "playlist.create.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val dbPlaylist = DbPlaylist {
                discordUserId = user.idLong
                name = arguments.name
            }

            db.playlists.add(dbPlaylist)

            log.info(
                msg = "Playlist created",
                context = mapOf(
                    "name" to arguments.name,
                    "user" to user.id
                )
            )

            respond {
                content = translate(
                    key = "playlist.create.response",
                    replacements = arrayOf(arguments.name)
                )
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.delete() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "delete"
        description = "playlist.delete.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                dbPlaylist.delete()

                log.info(
                    msg = "Player deleted",
                    context = mapOf(
                        "user" to user.id,
                        "name" to arguments.name
                    )
                )

                respond {
                    content = translate(
                        key = "playlist.delete.response",
                        replacements = arrayOf(dbPlaylist.name)
                    )
                }
            } else {
                respond {
                    content = translate(
                        key = "playlist.response.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.add() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistAddArgs) {
        name = "add"
        description = "playlist.add.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                val parseResult = IdentifierParser.toIdentifiers(arguments.music)
                val identifiers = parseResult.identifiers
                CoroutineScope(Dispatchers.IO).launch {
                    identifiers.forEach { parsedIdentifier ->
                        val addTrack: suspend (TrackResponse.PartialTrack) -> Unit = { track ->
                            val dbPlaylistSong = DbPlaylistSong {
                                playlistId = dbPlaylist.playlistId
                                title = track.info.title
                                uri = track.info.uri
                                identifier = track.info.identifier
                            }

                            db.playlistSongs.add(dbPlaylistSong)
                        }

                        val item = link.loadItem(parsedIdentifier)

                        when (item.loadType) {
                            TrackResponse.LoadType.TRACK_LOADED -> {
                                addTrack(item.track)
                            }
                            TrackResponse.LoadType.PLAYLIST_LOADED -> {
                                item.tracks.forEach { addTrack(it) }
                            }
                            TrackResponse.LoadType.SEARCH_RESULT -> {
                                addTrack(item.tracks.first())
                            }
                            else -> {}
                        }
                    }
                }

                respond {
                    content = translate(
                        key = "playlist.add.response",
                        replacements = arrayOf(identifiers.size, dbPlaylist.name)
                    )
                }
            } else {
                respond {
                    content = translate(
                        key = "playlist.response.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.remove() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistRemoveArgs) {
        name = "remove"
        description = "playlist.remove.description"
        check {
            anyGuild()
            hasDJRole()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                val dbPlaylistSong = db.playlistSongs.find {
                    (it.playlistId eq dbPlaylist.playlistId) and (it.playlistSongId eq arguments.songId.toLong())
                }

                if (dbPlaylistSong != null) {
                    dbPlaylistSong.delete()

                    respond {
                        content = translate(
                            key = "playlist.remove.response.success",
                            replacements = arrayOf(dbPlaylistSong.title, dbPlaylist.name)
                        )
                    }
                } else {
                    respond {
                        content = translate(
                            key = "playlist.remove.response.no-music",
                            replacements = arrayOf(arguments.songId)
                        )
                    }
                }
            } else {
                respond {
                    content = translate(
                        key = "playlist.response.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*>.queue() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "queue"
        description = "playlist.queue.description"
        check {
            anyGuild()
            hasDJRole()
            inVoiceChannel()
        }
        action {
            val identifiers = db.from(DbPlaylists)
                .leftJoin(DbPlaylistSongs, on = DbPlaylists.playlistId eq DbPlaylistSongs.playlistId)
                .select(DbPlaylistSongs.uri)
                .where { (DbPlaylists.name ilike arguments.name) and (DbPlaylists.discordUserId eq user.idLong) }
                .map { it.getString(1).toString() }
            val guild = guild!!.asGuild()
            val response = Jukebox.playLater(
                Jukebox.PlayRequest(
                    respond = {
                        respond { content = it }
                    },
                    respondMultiple = { choices, select -> select(choices.first()) },
                    parseResult = IdentifierParser.fromList(identifiers),
                    guild = guild,
                    mention = user.mention,
                    userId = user.id,
                    locale = getLocale()
                )
            )

            if (response.isNotBlank()) {
                respond { content = response }
            }
        }
    }
}

internal class PlaylistNameArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    val name by string {
        name = "name"
        description = tp.translate(
            key = "playlist.args.name.description",
            bundleName = TRANSLATION_BUNDLE
        )
    }
}

internal class PlaylistRemoveArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    val name by string {
        name = "name"
        description = tp.translate(
            key = "playlist.args.name.description",
            bundleName = TRANSLATION_BUNDLE
        )
    }
    val songId by int {
        name = "id"
        description = tp.translate(
            key = "playlist.args.id.description",
            bundleName = TRANSLATION_BUNDLE
        )
    }
}

internal class PlaylistAddArgs : KoinComponent, Arguments() {
    private val tp by inject<TranslationsProvider>()
    val name by string {
        name = "name"
        description = tp.translate(
            key = "playlist.args.name.description",
            bundleName = TRANSLATION_BUNDLE
        )
    }
    val music by string {
        name = "music"
        description = tp.translate(
            key = "playlist.args.music.description",
            bundleName = TRANSLATION_BUNDLE
        )
    }
}