package bogus.extension.administration.event

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.db.guilds
import bogus.util.asSnowflake
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberLeaveEvent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

fun createLeaveMessage(user: UserBehavior, guild: Guild): String {
    return "${user.mention} has left **${guild.name}**, very sadge."
}

suspend fun AdministrationExtension.leaveMessageEvent() {
    val db by inject<Database>()

    event<MemberLeaveEvent> {
        action {
            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq event.guild.idLong } ?: return@action
            val leaveChannelId = dbGuild.leaveChannelId ?: return@action
            val textChannel = event.guild.getChannelOf<TextChannel>(leaveChannelId.asSnowflake)

            textChannel.createMessage {
                content = createLeaveMessage(event.user, event.getGuild())
            }
        }
    }
}