package bogus.extension.anilist

import bogus.extension.anilist.db.users
import bogus.extension.anilist.embed.createMediaEmbed
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.Media
import bogus.extension.anilist.model.MediaList
import bogus.paginator.respondingStandardPaginator
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.common.Color
import dev.kord.core.behavior.GuildBehavior
import io.github.furstenheim.CopyDown
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.filter
import org.ktorm.entity.map

/**
 * Clean AniList profile description.
 */
fun String.aniClean(): String {
    return this
        .replace("~!", "")
        .replace("!~", "")
        .replace("~~~", "")
}

/**
 * Cleans down any html and converts them into a Markdown format.
 */
fun String.htmlToMarkdown(): String {
    val converter = CopyDown()
    return converter.convert(this)
}

/**
 * Cleans down any weird html from anilist and converts them into a Markdown format.
 */
fun String.weirdHtmlClean(): String {
    return this
        .replace("<i>", "_")
        .replace("</i>", "_")
        .replace("<b>", "**")
        .replace("</b>", "**")
}

/**
 * Converts AniList colors to their appropriate hex color.
 */
fun String.toHexColor() = when (this) {
    "blue" -> Color(0x3DB4F2)
    "purple" -> Color(0xC063FF)
    "green" -> Color(0x4CCA51)
    "orange" -> Color(0xEF881A)
    "red" -> Color(0xE13333)
    "pink" -> Color(0xFC9DD6)
    "gray" -> Color(0x677B94)
    else -> Color(0x000000)
}

/**
 * Converts this float to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Float.toStars(): String {
    return this.toInt().toStars()
}

/**
 * Converts this integer to a star rating system.
 *
 * 0 to 29: 1 star
 * 30 to 49: 2 stars
 * 50 to 69: 3 stars
 * 70 to 89: 4 stars
 * 90 and beyond: 5 stars
 */
fun Int.toStars(): String {
    if (this >= 90) {
        // 5 star
        return "★".repeat(5)
    } else if (this in 70..89) {
        // 4 star
        return "★".repeat(4)
    } else if (this in 50..69) {
        // 3 star
        return "★".repeat(3)
    } else if (this in 30..49) {
        // 2 star
        return "★".repeat(2)
    } else if (this in 1..29) {
        // 1 star
        return "★".repeat(1)
    }
    return "-"
}

object Util : KordExKoinComponent {
    val db by inject<Database>()
    val aniList by inject<AniList>()

    /**
     * Map AniList identifier to discord mention.
     */
    fun aniListToDiscordNameMap(userIds: List<Long>?): Map<Long, String?> {
        if (userIds == null || userIds.isEmpty()) return mapOf()
        return db.users
            .filter { it.aniListId inList userIds }
            .map {
                it.aniListId to "<@${it.discordId}>"
            }.toMap()
    }

    suspend fun lookupMediaList(
        medias: List<Media>?,
        guildId: Long?
    ): List<MediaList>? {
        val userIds = db.users
            .filter { it.discordGuildId eq (guildId ?: -1) }
            .map { it.aniListId }
        return aniList.findScoreByUsersAndMedias(
            userIds = userIds,
            mediaIds = medias?.map { it.id }
        )
    }
}

suspend fun ApplicationCommandContext.sendMediaResult(
    guild: GuildBehavior?,
    media: List<Media>
) {
    if (this !is PublicInteractionContext) return

    val mediaList = Util.lookupMediaList(media, guild?.id?.value?.toLong())
    val userIds = mediaList?.mapNotNull { it.user?.id }
    val aniToDiscordName = Util.aniListToDiscordNameMap(userIds)
    val paginator = respondingStandardPaginator(linkLabel = translate("find.link.label")) {
        timeoutSeconds = PAGINATOR_TIMEOUT
        media.forEach {
            page {
                apply(createMediaEmbed(it, mediaList, aniToDiscordName))
            }
        }
    }

    if (paginator.pages.groups.isNotEmpty()) {
        paginator.send()
    }
}
