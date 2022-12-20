package bogus.extension.music.command

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.constants.ITEMS_PER_CHUNK
import bogus.extension.music.*
import bogus.extension.music.MusicExtension.log
import bogus.extension.music.checks.inVoiceChannel
import bogus.extension.music.db.*
import bogus.extension.music.player.MusicTrack
import bogus.extension.music.player.TrackLoadType
import bogus.paginator.editingStandardPaginator
import bogus.util.abbreviate
import bogus.util.escapedBrackets
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.support.postgresql.ilike

suspend fun MusicExtension.playlist() {
    ephemeralSlashCommand {
        name = "command.playlist"
        description = "command.playlist.description"
        allowInDms = false

        list()
        show()
        create()
        delete()
        add()
        remove()
        queue()
    }
}

private suspend fun EphemeralSlashCommand<*, *>.list() {
    val db by inject<Database>()

    ephemeralSubCommand {
        name = "command.playlist.list"
        description = "command.playlist.list.description"
        check {
            anyGuild()
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
                songList = translate("response.playlist.list.empty")
            }

            respond {
                embed {
                    title = translate("response.playlist.list.title")
                    description = songList
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.show() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "command.playlist.show"
        description = "command.playlist.show.description"
        check {
            anyGuild()
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
                        key = "response.playlist.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.create() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "command.playlist.create"
        description = "command.playlist.create.description"
        check {
            anyGuild()
        }
        action {
            val dbPlaylist = DbPlaylist {
                discordUserId = user.idLong
                name = arguments.name
            }

            db.playlists.add(dbPlaylist)

            log.info {
                message = "Playlist created"
                context = mapOf(
                    "name" to arguments.name,
                    "user" to user.id
                )
            }

            respond {
                content = translate(
                    key = "response.playlist.create",
                    replacements = arrayOf(arguments.name)
                )
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.delete() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "command.playlist.delete"
        description = "command.playlist.delete.description"
        check {
            anyGuild()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                dbPlaylist.delete()

                log.info {
                    message = "Player deleted"
                    context = mapOf(
                        "user" to user.id,
                        "name" to arguments.name
                    )
                }

                respond {
                    content = translate(
                        key = "response.playlist.delete",
                        replacements = arrayOf(dbPlaylist.name)
                    )
                }
            } else {
                respond {
                    content = translate(
                        key = "response.playlist.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.add() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistAddArgs) {
        name = "command.playlist.add"
        description = "command.playlist.add.description"
        check {
            anyGuild()
        }
        action {
            val guild = guild ?: return@action
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                val parseResult = IdentifierParser.toIdentifiers(arguments.music)
                val identifiers = parseResult.identifiers
                CoroutineScope(Dispatchers.IO).launch {
                    val addedTracks = mutableListOf<MusicTrack>()

                    identifiers.forEach { parsedIdentifier ->
                        val addTrack: suspend (MusicTrack) -> Unit = { track ->
                            val dbPlaylistSong = DbPlaylistSong {
                                playlistId = dbPlaylist.playlistId
                                title = track.title
                                uri = track.uri
                                identifier = track.identifier
                            }

                            db.playlistSongs.add(dbPlaylistSong)
                        }

                        val item = guild.player.loader.loadItem(parsedIdentifier)

                        when (item.loadType) {
                            TrackLoadType.TRACK_LOADED -> {
                                addTrack(item.track)
                                addedTracks.add(item.track)
                            }
                            TrackLoadType.PLAYLIST_LOADED -> {
                                item.tracks.forEach {
                                    addTrack(it)
                                    addedTracks.add(item.track)
                                }
                            }
                            TrackLoadType.SEARCH_RESULT -> {
                                if (parseResult.spotify) {
                                    addTrack(item.tracks.first())
                                    addedTracks.add(item.tracks.first())
                                } else {
                                    respondChoices(item.tracks) { track ->
                                        addTrack(track)
                                        translate(
                                            key = "response.playlist.add.single",
                                            replacements = arrayOf(track.title, dbPlaylist.name)
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }

                    if (addedTracks.size == 1) {
                        respond {
                            content = translate(
                                key = "response.playlist.add.single",
                                replacements = arrayOf(addedTracks.first().title, dbPlaylist.name)
                            )
                        }
                    } else if (addedTracks.size > 1) {
                        respond {
                            content = translate(
                                key = "response.playlist.add",
                                replacements = arrayOf(addedTracks.size, dbPlaylist.name)
                            )
                        }
                    }
                }
            } else {
                respond {
                    content = translate(
                        key = "response.playlist.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.remove() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistRemoveArgs) {
        name = "command.playlist.remove"
        description = "command.playlist.remove.description"
        check {
            anyGuild()
        }
        action {
            val dbPlaylist = db.playlists.find {
                (it.discordUserId eq user.idLong) and (it.name ilike arguments.name)
            }

            if (dbPlaylist != null) {
                val dbPlaylistSong = db.playlistSongs.find {
                    (it.playlistId eq dbPlaylist.playlistId) and (it.playlistSongId eq arguments.musicId.toLong())
                }

                if (dbPlaylistSong != null) {
                    dbPlaylistSong.delete()

                    respond {
                        content = translate(
                            key = "response.playlist.remove",
                            replacements = arrayOf(dbPlaylistSong.title, dbPlaylist.name)
                        )
                    }
                } else {
                    respond {
                        content = translate(
                            key = "response.playlist.remove.no-music",
                            replacements = arrayOf(arguments.musicId)
                        )
                    }
                }
            } else {
                respond {
                    content = translate(
                        key = "response.playlist.no-playlist",
                        replacements = arrayOf(arguments.name)
                    )
                }
            }
        }
    }
}

private suspend fun EphemeralSlashCommand<*, *>.queue() {
    val db by inject<Database>()

    ephemeralSubCommand(::PlaylistNameArgs) {
        name = "command.playlist.queue"
        description = "command.playlist.queue.description"
        check {
            anyGuild()
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

private suspend fun AutoCompleteInteraction.suggestPlaylist(db: Database) {
    suggestString {
        val input = focusedOption.value
        if (input.isNotBlank()) {
            db.playlists.filter {
                (it.discordUserId eq user.idLong) and (it.name ilike "$input%")
            }
                .take(AUTOCOMPLETE_ITEMS_LIMIT)
                .forEach {
                    choice(it.name, it.name)
                }
        } else {
            db.playlists.filter {
                (it.discordUserId eq user.idLong)
            }
                .take(AUTOCOMPLETE_ITEMS_LIMIT)
                .forEach {
                    choice(it.name, it.name)
                }
        }
    }
}

private class PlaylistNameArgs : KordExKoinComponent, Arguments() {
    val db by inject<Database>()
    val name by coalescingString {
        name = "command.playlist.args.name"
        description = "command.playlist.args.name.description"

        autoComplete {
            suggestPlaylist(db)
        }
    }
}

private class PlaylistRemoveArgs : KordExKoinComponent, Arguments() {
    val db by inject<Database>()
    val name by coalescingString {
        name = "command.playlist.args.name"
        description = "command.playlist.args.name.description"

        autoComplete {
            suggestPlaylist(db)
        }
    }
    val musicId by int {
        name = "command.playlist.args.music-id"
        description = "command.playlist.args.music-id.description"
    }
}

private class PlaylistAddArgs : KordExKoinComponent, Arguments() {
    val db by inject<Database>()
    val name by coalescingString {
        name = "command.playlist.args.name"
        description = "command.playlist.args.name.description"

        autoComplete {
            suggestPlaylist(db)
        }
    }
    val music by string {
        name = "command.playlist.args.music"
        description = "command.playlist.args.music.description"
    }
}