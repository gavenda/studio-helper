package bogus.extension.anilist

import bogus.extension.anilist.coroutines.AiringSchedulePoller
import bogus.extension.anilist.db.guilds
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.first

suspend fun AniListExtension.notify(guild: GuildBehavior, poller: AiringSchedulePoller) {
    val db by inject<Database>()

    val dbGuild = db.guilds.first { it.discordGuildId eq guild.idLong }
    val airingSchedules = poller.poll()

    airingSchedules.forEach { airingSchedule ->
        val channel = guild.getChannelOf<GuildMessageChannel>(Snowflake(dbGuild.notificationChannelId))
        val mediaTitle = airingSchedule.media.title?.english ?: airingSchedule.media.title?.romaji

        if (channel.botHasPermissions(Permission.SendMessages, Permission.ViewChannel)) {
            channel.createEmbed {
                title = "New Episode Aired!"
                description = "Episode **${airingSchedule.episode}** of **$mediaTitle** has aired."
                color = Color(0xFF0000)
                thumbnail {
                    url = airingSchedule.media.coverImage?.extraLarge ?: ""
                }
            }
        }
    }
}