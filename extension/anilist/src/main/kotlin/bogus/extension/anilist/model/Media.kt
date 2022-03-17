package bogus.extension.anilist.model

import kotlinx.serialization.Serializable

/**
 * Anime or Manga
 */
@Serializable
data class Media(
    /**
     * The id of the media
     */
    val id: Long = 0,
    /**
     * The mal id of the media
     */
    val idMal: Long? = 0,
    /**
     * The official titles of the media in various languages
     */
    val title: bogus.extension.anilist.model.MediaTitle? = null,
    /**
     * The type of the media; anime or manga
     */
    val type: bogus.extension.anilist.model.MediaType = bogus.extension.anilist.model.MediaType.UNKNOWN,
    /**
     * The url for the media page on the AniList website
     */
    val siteUrl: String = "",
    /**
     * The amount of user's who have favourited the media
     */
    val favourites: Int = 0,
    /**
     * The number of users with the media on their list
     */
    val popularity: Int = 0,
    /**
     * Mean score of all the user's scores of the media
     */
    val meanScore: Int = 0,
    /**
     * A weighted average score of all the user's scores of the media
     */
    val averageScore: Int = 0,
    /**
     * Alternative titles of the media
     */
    val synonyms: List<String> = listOf(),
    /**
     * The genres of the media
     */
    val genres: List<String> = listOf(),
    /**
     * The ranking of the media in a particular time span and format compared to other media
     */
    val rankings: List<bogus.extension.anilist.model.MediaRank> = listOf(),
    /**
     * The banner image of the media
     */
    val bannerImage: String = "",
    /**
     * The cover images of the media
     */
    val coverImage: bogus.extension.anilist.model.MediaCoverImage? = null,
    /**
     * When the media's data was last updated
     */
    val updatedAt: Int = 0,
    /**
     * The amount of volumes the manga has when complete
     */
    val volumes: Int = 0,
    /**
     * The amount of chapters the manga has when complete
     */
    val chapters: Int = 0,
    /**
     * The general length of each anime episode in minutes
     */
    val duration: Int = 0,
    /**
     * The amount of episodes the anime has when complete
     */
    val episodes: Int = 0,
    /**
     * The year & season the media was initially released in
     */
    val seasonInt: Int = 0,
    /**
     * The season year the media was initially released in
     */
    val seasonYear: Int = 0,
    /**
     * The season the media was initially released in
     */
    val season: bogus.extension.anilist.model.MediaSeason = bogus.extension.anilist.model.MediaSeason.UNKNOWN,
    /**
     * Short description of the media's story and characters
     */
    val description: String = "",
    /**
     * The format the media was released in
     */
    val format: bogus.extension.anilist.model.MediaFormat = bogus.extension.anilist.model.MediaFormat.UNKNOWN,
    /**
     * The current releasing status of the media
     */
    val status: bogus.extension.anilist.model.MediaStatus = bogus.extension.anilist.model.MediaStatus.UNKNOWN
)
