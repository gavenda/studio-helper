package bogus.extension.anilist.embed

import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.MediaFormat
import bogus.extension.anilist.model.MediaListStatus
import bogus.extension.anilist.model.User
import bogus.extension.anilist.toHexColor
import bogus.util.abbreviate
import dev.kord.rest.builder.message.EmbedBuilder
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun User.createEmbed(): EmbedBuilder.() -> Unit = {
    val statistics = statistics ?: error("User statistics is null")
    val watchDuration = statistics.anime.minutesWatched.minutes
    val daysWatched = watchDuration.inWholeDays
    val hoursWatched = watchDuration.inWholeHours % 1.days.inWholeHours
    val minutesWatched = watchDuration.inWholeMinutes % 1.hours.inWholeMinutes
    val apostrophe = if (name.lowercase().endsWith("s")) "'" else "'s"

    val genres = (statistics.manga.genres + statistics.anime.genres)
    val tags = (statistics.anime.tags + statistics.manga.tags)

    val releaseYears = (statistics.manga.releaseYears + statistics.anime.releaseYears)
        .sortedByDescending { it.count }
        .distinctBy { it.releaseYear }

    val animeStartYears = statistics.anime.startYears
        .sortedByDescending { it.count }

    val mangaStartYears = statistics.manga.startYears
        .sortedByDescending { it.count }

    val statuses = (statistics.manga.statuses + statistics.anime.statuses)
        .sortedByDescending { it.count }
        .distinctBy { it.status }

    val formats = (statistics.anime.formats + statistics.manga.formats)
        .sortedByDescending { it.count }

    val weabTendencies = buildString {
        if (genres.size >= 3) {
            val genresByMean = genres
                .sortedByDescending { it.meanScore }
                .distinctBy { it.genre }
            val (g1, g2, g3) = genresByMean

            append("- Likes genres that are **${g1.genre}**/**${g2.genre}**/**${g3.genre}**.\n")
            append("- Seems to really hate **${genresByMean.last().genre}**.\n")

            statistics.anime
                .genres.maxByOrNull { it.minutesWatched }
                ?.let {
                    append("- Wasted **${it.minutesWatched}** minutes on **${it.genre}**.\n")
                }

            statistics.manga
                .genres.maxByOrNull { it.chaptersRead }
                ?.let {
                    append("- Wasted **${it.chaptersRead}** chapters on **${it.genre}**.\n")
                }
        }

        if (tags.size >= 3) {
            val tagsByMean = tags
                .sortedByDescending { it.meanScore }
                .distinctBy { it.tag.name }
            val (t1, t2, t3) = tagsByMean

            append("- Loves **${t1.tag.name}**/**${t2.tag.name}**/**${t3.tag.name}**.\n")
            append("- Absolutely hates **${tagsByMean.last().tag.name}**\n")

            statistics.anime
                .tags.maxByOrNull { it.minutesWatched }
                ?.let {
                    append("- Wasted **${it.minutesWatched}** minutes on **${it.tag.name}**.\n")
                }

            statistics.manga
                .tags.maxByOrNull { it.chaptersRead }
                ?.let {
                    append("- Wasted **${it.chaptersRead}** chapters on **${it.tag.name}**.\n")
                }
        }

        if (releaseYears.isNotEmpty()) {
            append("- Loves **${releaseYears[0].releaseYear}** media.\n")
        }

        if (animeStartYears.isNotEmpty()) {
            append("- Started consuming weabness in **${animeStartYears[0].startYear}**.\n")
        }

        if (mangaStartYears.isNotEmpty()) {
            append("- Started consuming trash in **${mangaStartYears[0].startYear}**.\n")
        }

        if (formats.isNotEmpty() && formats[0].format != MediaFormat.TV) {
            append("- Addicted to the **${formats[0].format}** format.\n")
        }

        if (statuses.isNotEmpty()) {
            val total = statuses
                .filter { it.status != MediaListStatus.PLANNING }
                .sumOf { it.count }
                .toDouble()

            val completed = statuses
                .filter { it.status == MediaListStatus.COMPLETED || it.status == MediaListStatus.REPEATING }
                .sumOf { it.count }
                .toDouble()

            val dropped = statuses
                .filter { it.status == MediaListStatus.DROPPED }
                .sumOf { it.count }

            val completedRatio = completed / total * 100
            val completedRatioStr = "%.2f".format(completedRatio)

            if (dropped == 0) {
                append("- Has **never** dropped an anime/manga!\n")
            }

            append("- Ends up completing $completedRatioStr%\n")

            if (statuses.first().status == MediaListStatus.PLANNING) {
                append("- Apparently thinks PLANNING > WATCHING...\n")
            }
        }
    }

    val userDescription = """
            ${about.aniClean().trim()}
            
            [**Anime List**](${siteUrl}/animelist)
            Total Entries: ${statistics.anime.count}
            Episodes Watched: ${statistics.anime.episodesWatched}
            Time Watched: $daysWatched Days - $hoursWatched Hours - $minutesWatched Minutes
            Mean Score: ${statistics.anime.count}
            
            [**Manga List**](${siteUrl}"/mangalist")
            Total Entries: ${statistics.manga.count}
            Volumes Read: ${statistics.manga.volumesRead}
            Chapters Read: ${statistics.manga.chaptersRead}
            Mean Score: ${statistics.manga.meanScore}
            
            [**Weab Tendencies**](${siteUrl}/stats/anime/overview)
            $weabTendencies
        """
        .trim()
        .trimIndent()
        .abbreviate(EmbedBuilder.Limits.description)

    title = "${name}${apostrophe} Statistics"
    description = userDescription.abbreviate(EmbedBuilder.Limits.description)
    color = options?.profileColor?.toHexColor()
    image = bannerImage
    thumbnail {
        url = avatar?.large ?: ""
    }
    url = siteUrl

    footer {
        text = "NOTE: Weab tendencies could be wrong since they are based on user data."
    }
}
