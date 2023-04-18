package bogus.extension.anilist.db

import bogus.extension.anilist.EXTENSION_NAME
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long

interface DbAiringAnime : Entity<DbAiringAnime> {
    companion object : Entity.Factory<DbAiringAnime>()

    val id: Long
    var discordGuildId: Long
    var userId: Long
    var mediaId: Long
}

object DbAiringAnimes : Table<DbAiringAnime>(
    tableName = "anilist_anime_airing_schedule"
) {
    val id = long("id").primaryKey().bindTo { it.id }
    val discordGuildId = long("discord_guild_id").bindTo { it.discordGuildId }
    val userId = long("discord_user_id").bindTo { it.userId }
    val mediaId = long("media_id").bindTo { it.mediaId }
}

val Database.airingAnimes get() = this.sequenceOf(DbAiringAnimes)