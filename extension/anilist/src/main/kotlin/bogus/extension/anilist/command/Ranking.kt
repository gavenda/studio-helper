package bogus.extension.anilist.command

import bogus.extension.anilist.AniListExtension
import bogus.extension.anilist.model.MediaFormat
import bogus.extension.anilist.model.MediaSeason
import bogus.extension.anilist.sendMediaResult
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingStringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

suspend fun AniListExtension.ranking() {
    publicSlashCommand(::RankingArgs) {
        name = "command.ranking"
        description = "command.ranking.description"
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
                amount = arguments.amount,
                formatIn = listOf(mediaFormat),
                season = mediaSeason,
                seasonYear = arguments.year,
                hentai = hentai
            )

            if (media == null || media.isEmpty()) {
                respond {
                    content = translate("ranking.error.no-criteria-results")
                }
            } else {
                sendMediaResult(guild, media)
            }
        }
    }
}

private class RankingArgs : Arguments() {
    val amount by defaultingInt {
        name = "command.ranking.args.amount"
        description = "command.ranking.args.amount.description"
        defaultValue = 10
    }
    val season by optionalString {
        name = "command.ranking.args.season"
        description = "command.ranking.args.season.description"
    }
    val year by optionalInt {
        name = "command.ranking.args.year"
        description = "command.ranking.args.year.description"
    }
    val format by defaultingStringChoice {
        name = "command.ranking.args.format"
        description = "command.ranking.args.format.description"
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