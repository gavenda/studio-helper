package bogus.extension.anilist.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long

interface DbAiringAnime : Entity<DbAiringAnime> {
    companion object : Entity.Factory<DbAiringAnime>()

    val id: Long
    var discordGuildId: Long
    var mediaId: Long
}

object DbAiringAnimes : Table<DbAiringAnime>("anime_airing_schedule") {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val mediaId = long("media_id").bindTo { it.mediaId }
}

val Database.airingAnimes get() = this.sequenceOf(DbAiringAnimes)