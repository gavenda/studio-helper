package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.graphql.AniList
import bogus.extension.anilist.model.MediaFormat
import bogus.extension.anilist.model.MediaSeason
import bogus.extension.anilist.sendMediaResult
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject

suspend fun AniListExtension.ranking() {
    val aniList by inject<AniList>()

    publicSlashCommand(::RankingArgs) {
        name = "ranking"
        description = "Shows the current ranking based on given parameters."
        action {
            log.info {
                message = "Looking up ranking"
                context = mapOf(
                    "arguments" to arguments,
                    "userId" to user.id
                )
            }

            val hentai = true
            val mediaSeason = arguments.season?.let { MediaSeason.valueOf(it) }
            val mediaFormat = MediaFormat.valueOf(arguments.format)

            val media = aniList.findMediaByRanking(
                amount = arguments.amount ?: 10,
                formatIn = listOf(mediaFormat),
                season = mediaSeason,
                seasonYear = arguments.year,
                hentai = hentai
            )

            if (media == null || media.isEmpty()) {
                respond {
                    content = translate("ranking.error.noResultsFromCriteria")
                }
            } else {
                sendMediaResult(guild, media)
            }
        }
    }
}

private class RankingArgs : Arguments() {
    val amount by optionalInt {
        name = "amount"
        description = "Number of media to show."
    }
    val season by optionalString {
        name = "season"
        description = "The media season."
    }
    val year by optionalInt {
        name = "year"
        description = "The media year."
    }
    val format by defaultingStringChoice {
        name = "format"
        description = "The media format."
        defaultValue = "TV"

        choice("Manga", "MANGA")
        choice("Movie", "MOVIE")
        choice("Music", "MUSIC")
        choice("Novel", "NOVEL")
        choice("ONA", "ONA")
        choice("Oneshot", "ONE_SHOT")
        choice("OVA", "OVA")
        choice("Special", "SPECIAL")
        choice("TV", "TV")
        choice("TV Short", "TV_SHORT")
    }
}