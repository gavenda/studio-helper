package bogus.extension.music.db

import bogus.extension.music.EXTENSION_NAME
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

interface DbGuild : Entity<DbGuild> {
    companion object : Entity.Factory<DbGuild>()

    var discordGuildId: Long
    var textChannelId: Long?
    var lastMessageId: Long?
    var volume: Int
}

object DbGuilds : Table<DbGuild>(
    tableName = "music_guild",
    schema = EXTENSION_NAME
) {
    val discordGuildId = long("discord_guild_id").primaryKey().bindTo { it.discordGuildId }
    val textChannelId = long("text_channel_id").bindTo { it.textChannelId }
    val lastMessageId = long("last_message_id").bindTo { it.lastMessageId }
    val volume = int("volume").bindTo { it.volume }
}

val Database.guilds get() = this.sequenceOf(DbGuilds)
