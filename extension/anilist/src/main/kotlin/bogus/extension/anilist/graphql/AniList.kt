package bogus.extension.anilist.graphql

interface AniList {

    /**
     * Finds an AniList user by their name, returning only their id and name.
     * @param name name to look up
     */
    suspend fun findUserByName(name: String): bogus.extension.anilist.model.User?

    /**
     * Find user statistics by their name.
     * @param name name to look up
     */
    suspend fun findUserStatisticsByName(name: String): bogus.extension.anilist.model.User?

    /**
     * Finds a media based on query.
     * @param query query name for media
     * @param type the type of media
     * @param hentai enable the hentai genre
     */
    suspend fun findMedia(
        query: String,
        type: bogus.extension.anilist.model.MediaType? = null,
        hentai: Boolean = false
    ): List<bogus.extension.anilist.model.Media>?

    /**
     * Finds a media based on its ranking.
     * @param amount the amount of results to return
     * @param formatIn the media formats to filter
     * @param season the season to filter
     * @param seasonYear the season year to filter
     * @param hentai enable the hentai genre
     */
    suspend fun findMediaByRanking(
        amount: Int,
        formatIn: List<bogus.extension.anilist.model.MediaFormat>?,
        season: bogus.extension.anilist.model.MediaSeason?,
        seasonYear: Int?,
        hentai: Boolean = false
    ): List<bogus.extension.anilist.model.Media>?

    suspend fun findMediaTitles(
        query: String,
        type: bogus.extension.anilist.model.MediaType? = null,
    ): List<String>

    suspend fun findCharacterNames(
        query: String
    ): List<String>

    suspend fun findStaffNames(
        query: String
    ): List<String>

    suspend fun findUserNames(query: String): List<String>

    /**
     * Finds an AniList user score based on the given users and medias.
     * @param userIds user ids for related users
     * @param mediaIds media ids for related medias
     */
    suspend fun findScoreByUsersAndMedias(
        userIds: List<Long>?,
        mediaIds: List<Long>?
    ): List<bogus.extension.anilist.model.MediaList>?

    /**
     * Finds a character based on query.
     * @param query the character query
     */
    suspend fun findCharacter(query: String?): List<bogus.extension.anilist.model.Character>?

    /**
     * Finds an anime/media staff based on query.
     * @param query the character query
     */
    suspend fun findStaff(query: String?): List<bogus.extension.anilist.model.Staff>?
}