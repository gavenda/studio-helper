package bogus.extension.anilist.embed

import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.MediaFormat
import bogus.extension.anilist.model.MediaListStatus
import bogus.extension.anilist.model.User
import bogus.extension.anilist.toHexColor
import bogus.extension.anilist.toStars
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
        val genresByMean = genres
            .sortedByDescending { it.meanScore }
            .distinctBy { it.genre }
        val (g1, g2, g3) = genresByMean
        val worseAnimeGenre = statistics.anime.genres
            .sortedByDescending { it.meanScore }
            .take(30)
            .last()
        val worseMangaGenre = statistics.manga.genres
            .sortedByDescending { it.meanScore }
            .take(30)
            .last()

        field {
            name = "Top Genres"
            value = """
                - ${g1.genre} (${g1.meanScore.toStars()})
                - ${g2.genre} (${g2.meanScore.toStars()})
                - ${g3.genre} (${g3.meanScore.toStars()})
            """.trimIndent()
        }

        field {
            name = "Most Hated Genres"
            value = """
                - ${worseAnimeGenre.genre} (${worseAnimeGenre.meanScore.toStars()})
                - ${worseMangaGenre.genre} (${worseMangaGenre.meanScore.toStars()})
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
        val tagsByMean = tags
            .sortedByDescending { it.meanScore }
            .distinctBy { it.tag.name }
        val (t1, t2, t3) = tagsByMean
        val worseAnimeTag = statistics.anime.tags
            .sortedByDescending { it.meanScore }
            .take(30)
            .last()
        val worseMangaTag = statistics.manga.tags
            .sortedByDescending { it.meanScore }
            .take(30)
            .last()

        field {
            name = "Top Tags"
            value = """
                - ${t1.tag.name} (${t1.meanScore.toStars()})
                - ${t2.tag.name} (${t2.meanScore.toStars()})
                - ${t3.tag.name} (${t3.meanScore.toStars()})
            """.trimIndent()
        }

        field {
            name = "Most Hated Tags"
            value = """
                - ${worseAnimeTag.tag.name} (${worseAnimeTag.meanScore.toStars()})
                - ${worseMangaTag.tag.name} (${worseMangaTag.meanScore.toStars()})
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
