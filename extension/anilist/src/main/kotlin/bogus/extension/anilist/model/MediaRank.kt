package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * The ranking of a media in a particular time span and format compared to other media
 */
@Serializable
data class MediaRank(
    /**
     * The id of the rank
     */
    val id: Int = 0,
    /**
     * The numerical rank of the media
     */
    val rank: Int = 0,
    /**
     * The type of ranking
     */
    val type: bogus.extension.anilist.model.MediaRankType = bogus.extension.anilist.model.MediaRankType.UNKNOWN,
    /**
     * The format the media is ranked within
     */
    val format: bogus.extension.anilist.model.MediaFormat = bogus.extension.anilist.model.MediaFormat.UNKNOWN,
    /**
     * The year the media is ranked within
     */
    val year: Int = 0,
    /**
     * The season the media is ranked within
     */
    val season: bogus.extension.anilist.model.MediaSeason = bogus.extension.anilist.model.MediaSeason.UNKNOWN,
    /**
     * If the ranking is based on all time instead of a season/year
     */
    val allTime: Boolean = false,
    /**
     * String that gives context to the ranking type and time span
     */
    val context: String = "",
)
