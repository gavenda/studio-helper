package bogus.extension.anilist.embed

import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.MediaFormat
import bogus.extension.anilist.model.MediaListStatus
import bogus.extension.anilist.model.User
import bogus.extension.anilist.toHexColor
import bogus.extension.anilist.toStars
import bogus.util.abbreviate
import dev.kord.rest.builder.message.EmbedBuilder
import kotlin.math.ceil
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class TopStats(
    val name: String,
    val count: Int,
    val meanScore: Float
)

fun User.createEmbed(): EmbedBuilder.() -> Unit = {
    val statistics = statistics ?: error("User statistics is null")
    val watchDuration = statistics.anime.minutesWatched.minutes
    val daysWatched = watchDuration.inWholeDays
    val hoursWatched = watchDuration.inWholeHours % 1.days.inWholeHours
    val minutesWatched = watchDuration.inWholeMinutes % 1.hours.inWholeMinutes
    val apostrophe = if (name.lowercase().endsWith("s")) "'" else "'s"

    val genres = (statistics.manga.genres + statistics.anime.genres)
    val tags = (statistics.anime.tags + statistics.manga.tags)

    // Combined medians
    val genresByMean = genres.distinctBy { it.genre }
        .map { stats ->
            val anime = statistics.anime.genres.firstOrNull { it.genre == stats.genre }
            val manga = statistics.manga.genres.firstOrNull { it.genre == stats.genre }

            if (anime != null && manga != null) {
                // Calculate mean score of both groups
                val count = (anime.count + manga.count)
                val meanScore = ((anime.meanScore * anime.count) + (manga.meanScore * manga.count)) / count
                return@map TopStats(anime.genre, count, ceil(meanScore))
            } else {
                val animeOrManga = anime ?: manga ?: error("Both anime and manga are null, data error from AniList?")
                return@map TopStats(animeOrManga.genre, animeOrManga.count, animeOrManga.meanScore)
            }
        }
        .sortedByDescending { it.meanScore }

    val tagsByMean = tags.distinctBy { it.tag.name }
        .map { stats ->
            val anime = statistics.anime.tags.firstOrNull { it.tag.name == stats.tag.name }
            val manga = statistics.manga.tags.firstOrNull { it.tag.name == stats.tag.name }

            if (anime != null && manga != null) {
                // Calculate mean score of both groups
                val count = (anime.count + manga.count)
                val meanScore = ((anime.meanScore * anime.count) + (manga.meanScore * manga.count)) / count
                return@map TopStats(anime.tag.name, count, ceil(meanScore))
            } else {
                val animeOrManga = anime ?: manga ?: error("Both anime and manga are null, data error from AniList?")
                return@map TopStats(animeOrManga.tag.name, animeOrManga.count, animeOrManga.meanScore)
            }
        }
        .sortedByDescending { it.meanScore }

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

    field {
        name = "Anime"
        value = """
            - Total: **${statistics.anime.count}**
            - Episodes: **${statistics.anime.episodesWatched}**
            - Time: **$daysWatched** days, **$hoursWatched hours**, **$minutesWatched** minutes
            - Mean Score: **${statistics.anime.count}**
        """.trimIndent()
    }

    field {
        name = "Manga"
        value = """
            - Total: **${statistics.manga.count}**
            - Volumes: **${statistics.manga.volumesRead}**
            - Chapters: **${statistics.manga.chaptersRead}**
            - Mean Score: **${statistics.manga.meanScore}**
        """.trimIndent()
    }

    field {
        name = "Weab Tendencies"
        value = buildString {
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

                append("- Ends up completing **$completedRatioStr%**.\n")

                if (statuses.first().status == MediaListStatus.PLANNING) {
                    append("- Apparently thinks PLANNING > WATCHING...\n")
                }
            }
        }
    }

    if (genres.size >= 3) {
        val (s1, s2, s3) = genresByMean
        val worseGenre = genresByMean
            .filter { it.meanScore > 0 }
            .minBy { it.meanScore }

        field {
            name = "Top Genres"
            value = """
                - ${s1.name} (Score: ${s1.meanScore}, Count: ${s1.count})
                - ${s2.name} (Score: ${s2.meanScore}, Count: ${s2.count})
                - ${s3.name} (Score: ${s3.meanScore}, Count: ${s3.count})
            """.trimIndent()
        }

        field {
            name = "Most Hated Genre"
            value = """
                - ${worseGenre.name} (Score: ${worseGenre.meanScore}, Count: ${worseGenre.count})
            """.trimIndent()
        }

        val genreStats = buildString {
            statistics.anime
                .genres.maxByOrNull { it.minutesWatched }
                ?.let { append("- Wasted **${it.minutesWatched}** minutes on **${it.genre}**.\n") }
            statistics.manga
                .genres.maxByOrNull { it.chaptersRead }
                ?.let {
                    append("- Wasted **${it.chaptersRead}** chapters on **${it.genre}**.\n")
                }
        }.dropLast(1)

        if (genreStats.isNotBlank()) {
            field {
                name = "Genre Stats"
                value = genreStats
            }
        }
    }

    if (tags.size >= 3) {
        val (s1, s2, s3) = tagsByMean
        val worseTag = tagsByMean
            .filter { it.meanScore > 0 }
            .minBy { it.meanScore }

        field {
            name = "Top Tags"
            value = """
                - ${s1.name} (Score: ${s1.meanScore}, Count: ${s1.count})
                - ${s2.name} (Score: ${s2.meanScore}, Count: ${s2.count})
                - ${s3.name} (Score: ${s3.meanScore}, Count: ${s3.count})
            """.trimIndent()
        }

        field {
            name = "Most Hated Tag"
            value = """
                - ${worseTag.name} (Score: ${worseTag.meanScore}, Count: ${worseTag.count})
            """.trimIndent()
        }

        val tagStats = buildString {
            statistics.anime
                .tags.maxByOrNull { it.minutesWatched }
                ?.let { append("- Wasted **${it.minutesWatched}** minutes on **${it.tag.name}**.\n") }
            statistics.manga
                .tags.maxByOrNull { it.chaptersRead }
                ?.let {
                    append("- Wasted **${it.chaptersRead}** chapters on **${it.tag.name}**.\n")
                }
        }.dropLast(1)

        if (tagStats.isNotBlank()) {
            field {
                name = "Tag Stats"
                value = tagStats
            }
        }
    }

    title = "${name}${apostrophe} Statistics"
    description = about.aniClean().trim().abbreviate(EmbedBuilder.Limits.description)
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
