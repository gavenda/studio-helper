package bogus.extension.administration.db

import bogus.extension.administration.EXTENSION_NAME
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long

interface DbGuild : Entity<DbGuild> {
    companion object : Entity.Factory<DbGuild>()

    var discordGuildId: Long
    var welcomeChannelId: Long?
    var leaveChannelId: Long?
}

object DbGuilds : Table<DbGuild>(
    tableName = "guild",
    schema = EXTENSION_NAME
) {
    val discordGuildId = long("discord_guild_id").primaryKey().bindTo { it.discordGuildId }
    val welcomeChannelId = long("welcome_channel_id").bindTo { it.welcomeChannelId }
    val leaveChannelId = long("leave_channel_id").bindTo { it.leaveChannelId }
}

val Database.guilds get() = this.sequenceOf(DbGuilds)
