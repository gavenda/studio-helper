package bogus.extension.anilist

import bogus.extension.anilist.coroutines.AiringSchedulePoller
import bogus.extension.anilist.db.airingAnimes
import bogus.extension.anilist.db.guilds
import bogus.util.asFMTLogger
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import mu.KotlinLogging
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.first
import org.ktorm.entity.map

class NotifyScheduler(val kord: Kord) : KordExKoinComponent {
    private val db by inject<Database>()
    private val log = KotlinLogging.logger { }.asFMTLogger()
    private val pollers = mutableMapOf<Snowflake, AiringSchedulePoller>()
    private val kronScheduler = buildSchedule {
        minutes {
            from(0) every 30
        }
    }

    suspend fun start() = kronScheduler.doInfinity {
        pollers.forEach { (guildId, poller) ->
            val guild = kord.getGuild(guildId, EntitySupplyStrategy.cache) ?: return@forEach
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
    }

    fun remove(guildId: Snowflake, mediaId: Long) {
        val poller = pollers[guildId] ?: return

        if (poller.mediaIds.isEmpty()) {
            pollers.remove(guildId)
        }

        poller.removeMediaId(mediaId)
    }

    fun schedule(guild: GuildBehavior) {
        val mediaIds = db.airingAnimes
            .filter { it.discordGuildId eq guild.idLong }
            .map { it.mediaId }
        if (mediaIds.isNotEmpty()) {
            pollers.computeIfAbsent(guild.id) { AiringSchedulePoller(mediaIds) }
                .updateMediaIds(mediaIds)
            return
        }

        log.warn {
            message = "No media id given, will not poll"
            context = mapOf(
                "guildId" to guild.id
            )
        }
    }

}