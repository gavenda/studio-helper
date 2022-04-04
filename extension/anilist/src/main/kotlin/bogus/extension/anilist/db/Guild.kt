package bogus.extension.anilist.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface DbGuild : Entity<DbGuild> {
    companion object : Entity.Factory<DbGuild>()

    val id: Long
    var discordGuildId: Long
    var hentai: Boolean
    var locale: String
    var notificationChannelId: Long
}

object DbGuilds : Table<DbGuild>("guild") {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val hentai = boolean("hentai").bindTo { it.hentai }
    val locale = varchar("locale").bindTo { it.locale }
    val notificationChannelId = long("notification_channel_id").bindTo { it.notificationChannelId }
}

val Database.guilds get() = this.sequenceOf(DbGuilds)