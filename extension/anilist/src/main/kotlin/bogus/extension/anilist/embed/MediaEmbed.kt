package bogus.extension.anilist.embed

import bogus.extension.anilist.EmbedMedia
import bogus.extension.anilist.htmlToMarkdown
import bogus.extension.anilist.model.*
import bogus.extension.anilist.toStars
import bogus.util.abbreviate
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

fun createMediaEmbed(
    media: Media,
    mediaList: List<MediaList>?,
    aniToDiscordName: Map<Long, String?>,
): EmbedBuilder.() -> Unit = {
    val season =
        if (media.season != MediaSeason.UNKNOWN) media.season.displayName else "-"
    val seasonYear = if (media.seasonYear != 0) media.seasonYear else "-"
    val score = media.meanScore.toStars()
    var duration = if (media.duration != 0) "${media.duration} minutes" else "-"
    val episodes = if (media.episodes != 0) "${media.episodes}" else "-"
    val episodicFormats = listOf(
        MediaFormat.ONA,
        MediaFormat.OVA,
        MediaFormat.TV,
        MediaFormat.SPECIAL
    )

    val completed = StringBuilder()
    val planned = StringBuilder()
    val inProgress = StringBuilder()
    val paused = StringBuilder()
    val dropped = StringBuilder()
    val notOnList = StringBuilder()
    val repeating = StringBuilder()

    mediaList?.filter { it.mediaId == media.id }?.map { ml ->
        EmbedMedia(
            discordName = aniToDiscordName[ml.user?.id],
            status = ml.status,
            score = ml.score,
            progress = ml.progress
        )
    }?.sortedWith(compareBy({ it.progress }, { it.discordName }))?.forEach { embedMedia ->
        when (embedMedia.status) {
            MediaListStatus.COMPLETED -> {
                if (embedMedia.score == 0f) {
                    // Display a ? if no score. (0 indicates no score on AniList)
                    completed.append("- ${embedMedia.discordName} ‣ (-)\n")
                } else {
                    // Display the score otherwise.
                    completed.append("- ${embedMedia.discordName} ‣ ${embedMedia.score.toStars()} (${embedMedia.score})\n")
                }
            }
            MediaListStatus.CURRENT -> {
                inProgress.append("- ${embedMedia.discordName} ‣ [${embedMedia.progress}]\n")
            }
            MediaListStatus.DROPPED -> {
                dropped.append("- ${embedMedia.discordName} ‣ [${embedMedia.progress}]\n")
            }
            MediaListStatus.PAUSED -> {
                paused.append("- ${embedMedia.discordName} ‣ [${embedMedia.progress}]\n")
            }
            MediaListStatus.PLANNING -> {
                planned.append("- ${embedMedia.discordName}\n")
            }
            MediaListStatus.REPEATING -> {
                if (embedMedia.score == 0f) {
                    completed.append("- ${embedMedia.discordName} ‣ (-)\n")
                }
                // Display the score otherwise.
                else {
                    repeating.append("- ${embedMedia.discordName} ‣ ${embedMedia.score.toStars()} (${embedMedia.score}) [Episode: ${embedMedia.progress}]\n")
                }
            }
            else -> {
                notOnList.append("- ${embedMedia.discordName}\n")
            }
        }
    }

    // Add 'per episode' for TV, OVA, ONA and Specials.
    if (duration != "-" && episodicFormats.contains(media.format)) {
        duration += " per episode"
    }

    title = media.title?.english ?: media.title?.romaji
    description = buildString {
        if (media.title?.romaji != null && media.title.english != null) {
            append("_(Romaji: ${media.title.romaji})_\n")
        }
        if (media.title?.native != null) {
            append("_(Native: ${media.title.native})_\n")
        }

        val actualDescription = media.description
            .htmlToMarkdown()
            .abbreviate(EmbedBuilder.Limits.description)

        append("\n")
        append(actualDescription)
    }
        .abbreviate(EmbedBuilder.Limits.description)
        .dropLastWhile { it != '\n' }

    thumbnail {
        url = media.coverImage?.extraLarge ?: ""
    }

    image = media.bannerImage
    url = media.siteUrl
    color = Color(0xFF0000)

    // First row
    field {
        name = "Type"
        value = media.type.displayName
        inline = true
    }
    field {
        name = "Status"
        value = media.status.displayName
        inline = true
    }

    if (season == "-" && seasonYear == "-") {
        field {
            name = "Season"
            value = "?"
            inline = true
        }
    } else {
        field {
            name = "Season"
            value = "$season $seasonYear"
            inline = true
        }
    }

    // Second row
    field {
        name = "Rating"
        value = "$score (${media.meanScore})"
        inline = true
    }
    field {
        name = "Popularity"
        value = "${media.popularity}"
        inline = true
    }
    field {
        name = "Favorites"
        value = "${media.favourites}"
        inline = true
    }

    // Third row
    field {
        name = "Episodes"
        value = episodes
        inline = true
    }
    field {
        name = "Duration"
        value = duration
        inline = true
    }
    field {
        name = "Format"
        value = media.format.displayName
        inline = true
    }

    if (media.genres.isNotEmpty()) {
        // Fourth row
        field {
            name = "Genres"
            value = media.genres.joinToString(
                separator = " - "
            ) {
                "`$it`"
            }
            inline = false
        }
    }

    // User scores
    if (paused.isNotBlank()) {
        field {
            name = "Paused"
            value = paused.toString()
            inline = false
        }
    }

    if (inProgress.isNotBlank()) {
        field {
            name = "In Progress"
            value = inProgress.toString()
            inline = false
        }
    }

    if (repeating.isNotBlank()) {
        field {
            name = "Rewatching"
            value = repeating.toString()
            inline = false
        }
    }

    if (completed.isNotBlank()) {
        field {
            name = "Completed"
            value = completed.toString()
            inline = false
        }
    }

    if (dropped.isNotBlank()) {
        field {
            name = "Dropped"
            value = dropped.toString()
            inline = false
        }
    }

    if (planned.isNotBlank()) {
        field {
            name = "Planned"
            value = planned.toString()
            inline = false
        }
    }

    if (notOnList.isNotBlank()) {
        field {
            name = "Not On List"
            value = notOnList.toString()
            inline = false
        }
    }

    author {
        name = buildString {
            val mediaRankAscending = media.rankings
                .sortedBy { it.rank }

            val allTimeRank = mediaRankAscending
                .firstOrNull {
                    it.type == MediaRankType.RATED && it.allTime
                }
            val seasonRank = mediaRankAscending
                .firstOrNull {
                    it.type == MediaRankType.RATED && !it.allTime && it.season != MediaSeason.UNKNOWN
                }

            if (allTimeRank != null) {
                append("Rank #${allTimeRank.rank} (${media.format.displayName})")
            }
            if (seasonRank != null) {
                if (allTimeRank != null) {
                    append(" ${Typography.bullet} ")
                }

                append("Rank #${seasonRank.rank} (${media.format.displayName}) of ${seasonRank.season.displayName} ${seasonRank.year}")
            }
        }
    }
}

