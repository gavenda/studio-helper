package bogus.extension.administration.event

import bogus.extension.administration.AdministrationExtension
import bogus.extension.administration.db.guilds
import bogus.util.asSnowflake
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberJoinEvent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.firstOrNull

fun createWelcomeMessage(member: MemberBehavior, guild: Guild): String {
    return "Welcome ${member.mention} to **${guild.name}**!"
}

suspend fun AdministrationExtension.welcomeMessageEvent() {
    val db by inject<Database>()

    event<MemberJoinEvent> {
        action {
            val dbGuild = db.guilds.firstOrNull { it.discordGuildId eq event.guild.idLong } ?: return@action
            val welcomeChannelId = dbGuild.welcomeChannelId ?: return@action
            val textChannel = event.guild.getChannelOf<TextChannel>(welcomeChannelId.asSnowflake)

            textChannel.createMessage {
                content = createWelcomeMessage(event.member, event.getGuild())
            }
        }
    }
}