package bogus.extension.music.db

import bogus.extension.music.EXTENSION_NAME
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface DbPlaylist : Entity<DbPlaylist> {
    companion object : Entity.Factory<DbPlaylist>()

    var playlistId: Long
    var discordUserId: Long
    var name: String
}

interface DbPlaylistSong : Entity<DbPlaylistSong> {
    companion object : Entity.Factory<DbPlaylistSong>()

    var playlistSongId: Long
    var playlistId: Long
    var title: String
    var uri: String
    var identifier: String
}

object DbPlaylistSongs : Table<DbPlaylistSong>(
    tableName = "music_playlist_song"
) {
    val playlistSongId = long("playlist_song_id").primaryKey().bindTo { it.playlistSongId }
    val playlistId = long("playlist_id").primaryKey().bindTo { it.playlistId }
    val title = varchar("title").bindTo { it.title }
    val uri = varchar("uri").bindTo { it.uri }
    val identifier = varchar("identifier").bindTo { it.identifier }
}

object DbPlaylists : Table<DbPlaylist>(
    tableName = "music_playlist"
) {
    val playlistId = long("playlist_id").primaryKey().bindTo { it.playlistId }
    val discordUserId = long("discord_user_id").bindTo { it.discordUserId }
    val name = varchar("name").bindTo { it.name }
}

val Database.playlists get() = this.sequenceOf(DbPlaylists)
val Database.playlistSongs get() = this.sequenceOf(DbPlaylistSongs)
