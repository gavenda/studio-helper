package bogus.extension.counter.db

import bogus.extension.counter.EXTENSION_NAME
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant
import java.util.Date

interface DbGuildCount : Entity<DbGuildCount> {
    companion object : Entity.Factory<DbGuildCount>()

    var guildCountId: Long
    var discordGuildId: Long
    var countName: String
    var countAmount: Long
    var lastUserId: Long
    var lastTimestamp: Instant
}

object DbGuildCounts : Table<DbGuildCount>(
    tableName = "guild_count",
    schema = EXTENSION_NAME
) {
    val guildCountId = long("guild_count_id").primaryKey().bindTo { it.guildCountId }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val countName = varchar("count_name").bindTo { it.countName }
    val countAmount = long("count_amount").bindTo { it.countAmount }
    val lastUserId = long("last_user_id").bindTo { it.lastUserId }
    val lastTimestamp = timestamp("last_timestamp").bindTo { it.lastTimestamp }
}

val Database.counts get() = this.sequenceOf(DbGuildCounts)
