package bogus.extension.anilist.embed

import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.MediaListStatus
import bogus.extension.anilist.model.User
import bogus.extension.anilist.toHexColor
import bogus.util.abbreviate
import dev.kord.rest.builder.message.EmbedBuilder
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun createUserEmbed(user: User): EmbedBuilder.() -> Unit = {
    val statistics = user.statistics ?: error("User statistics is null")
    val d = statistics.anime.minutesWatched.minutes
    val apostrophe = if (user.name.lowercase().endsWith("s")) "'" else "'s"

    val genres = (statistics.manga.genres + statistics.anime.genres)
        .sortedByDescending { it.count }
        .distinctBy { it.genre }

    val tags = (statistics.anime.tags + statistics.manga.tags)
        .sortedByDescending { it.count }
        .distinctBy { it.tag.id }

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

    val formats = statistics.anime.formats
        .sortedByDescending { it.count }

    val weabTendencies = StringBuilder()

    if (genres.size >= 3) {
        val genresByMeanScore = genres
            .sortedByDescending { it.meanScore }
        val worseGenre = genresByMeanScore.last()

        // they could have watched zero anime, assume null
        val animeMinutes = statistics.anime
            .genres.maxByOrNull { it.minutesWatched }
        // same with this, didn't read a single shit, assume null
        val mangaMinutes = statistics.manga
            .genres.maxByOrNull { it.chaptersRead }

        weabTendencies.append("- Is a **${genresByMeanScore[0].genre}**/**${genresByMeanScore[1].genre}**/**${genresByMeanScore[2].genre}** normie.\n")
        weabTendencies.append("- Seems to hate **${worseGenre.genre}** based on mean score.\n")

        if (animeMinutes != null && animeMinutes.minutesWatched > 0) {
            weabTendencies.append("- Wasted **${animeMinutes.minutesWatched}** minutes on **${animeMinutes.genre}**.\n")
        }
        if (mangaMinutes != null && mangaMinutes.minutesWatched > 0) {
            weabTendencies.append("- Wasted **${mangaMinutes.chaptersRead}** chapters on **${mangaMinutes.genre}**.\n")
        }
    }

    if (tags.size >= 3) {
        val tagsByMeanScore = tags
            .sortedByDescending { it.meanScore }
        val worseTag = tags.last()

        // they could have watched zero anime, assume null
        val animeMinutes = statistics.anime
            .tags.maxByOrNull { it.minutesWatched }
        // same with this, didn't read a single shit, assume null
        val mangaMinutes = statistics.manga
            .tags.maxByOrNull { it.chaptersRead }


        weabTendencies.append("- Loves **${tagsByMeanScore[0].tag.name}**/**${tagsByMeanScore[1].tag.name}**/**${tagsByMeanScore[2].tag.name}** media.\n")
        weabTendencies.append("- Seems to hate **${worseTag.tag.name}** based on mean score.\n")

        if (animeMinutes != null && animeMinutes.minutesWatched > 0) {
            weabTendencies.append("- Wasted **${animeMinutes.minutesWatched}** minutes on **${animeMinutes.tag.name}**.\n")
        }
        if (mangaMinutes != null && mangaMinutes.minutesWatched > 0) {
            weabTendencies.append("- Wasted **${mangaMinutes.chaptersRead}** chapters on **${mangaMinutes.tag.name}**.\n")
        }
    }

    if (releaseYears.isNotEmpty()) {
        weabTendencies.append("- Loves **${releaseYears[0].releaseYear}** media.\n")
    }

    if (animeStartYears.isNotEmpty()) {
        weabTendencies.append("- Started consuming weabness in **${animeStartYears[0].startYear}**.\n")
    }

    if (mangaStartYears.isNotEmpty()) {
        weabTendencies.append("- Started consuming trash in **${mangaStartYears[0].startYear}**.\n")
    }

    if (formats.isNotEmpty()) {
        weabTendencies.append("- Addicted to the **${formats[0].format}** format.\n")
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
            weabTendencies.append("- Has **never** dropped an anime/manga!\n")
        }

        weabTendencies.append("- Ends up completing $completedRatioStr%\n")

        if (statuses.first().status == MediaListStatus.PLANNING) {
            weabTendencies.append("- Apparently thinks PLANNING > WATCHING...\n")
        }
    }

    val userDescription = """
            ${user.about.aniClean().trim()}
            
            [**Anime List**](${user.siteUrl}/animelist)
            Total Entries: ${statistics.anime.count}
            Episodes Watched: ${statistics.anime.episodesWatched}
            Time Watched: ${d.inWholeDays} Days - ${d.inWholeHours} Hours - ${d.inWholeMinutes} Minutes
            Mean Score: ${statistics.anime.count}
            
            [**Manga List**](${user.siteUrl}"/mangalist")
            Total Entries: ${statistics.manga.count}
            Volumes Read: ${statistics.manga.volumesRead}
            Chapters Read: ${statistics.manga.chaptersRead}
            Mean Score: ${statistics.manga.meanScore}
            
            [**Weab Tendencies**](${user.siteUrl}/stats/anime/overview)
            $weabTendencies
        """
        .trim()
        .trimIndent()
        .abbreviate(EmbedBuilder.Limits.description)

    title = "${user.name}${apostrophe} Statistics"
    description = userDescription.abbreviate(EmbedBuilder.Limits.description)
    color = user.options?.profileColor?.toHexColor()
    image = user.bannerImage
    thumbnail {
        url = user.avatar?.large ?: ""
    }
    url = user.siteUrl

    footer {
        text = "NOTE: Weab tendencies could be wrong since they are based on user data."
    }
}
