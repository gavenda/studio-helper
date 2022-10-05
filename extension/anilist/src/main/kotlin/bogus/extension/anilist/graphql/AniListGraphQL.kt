package bogus.extension.anilist.graphql

import bogus.extension.anilist.model.*
import bogus.util.findResourceAsText
import kotlinx.serialization.Serializable

class AniListGraphQL : AniList {
    private val graphUri = "https://graphql.anilist.co"

    override suspend fun findUserByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindUserByName.graphql")
        val variables = FindUser(name)
        val result = GQL.query<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findUserStatisticsByName(name: String): User? {
        val gqlQuery = findResourceAsText("/gql/FindStatisticsByUserName.graphql")
        val variables = FindUser(name)
        val result = GQL.query<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findMedia(
        query: String,
        type: MediaType?,
        hentai: Boolean
    ): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMedia.graphql")
        val variables = FindMedia(
            query = query,
            type = type,
            page = 1,
            perPage = 10,
            genreNotIn = if (!hentai) listOf("Hentai") else null
        )
        val result = GQL.query<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByRanking(
        amount: Int,
        formatIn: List<MediaFormat>?,
        season: MediaSeason?,
        seasonYear: Int?,
        hentai: Boolean
    ): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByRanking.graphql")
        val variables = FindMedia(
            page = 1,
            perPage = amount,
            sort = listOf(MediaSort.SCORE_DESC),
            formatIn = formatIn,
            season = season,
            seasonYear = seasonYear,
            genreNotIn = if (!hentai) listOf("Hentai") else null
        )
        val result = GQL.query<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaTitlesAsString(query: String, type: MediaType?): List<String> {
        return buildList {
            findMediaTitles(query, type)?.forEach {
                it.title?.native?.let { title -> add(title) }
                it.title?.romaji?.let { title -> add(title) }
                it.title?.english?.let { title -> add(title) }
                addAll(it.synonyms)
            }
        }.distinct()
    }

    override suspend fun findMediaTitles(query: String, type: MediaType?): List<Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaName.graphql")
        val variables = FindMedia(
            query = query,
            type = type,
            page = 1,
            perPage = 10
        )
        val result = GQL.query<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findCharacterNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindCharacterName.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = GQL.query<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.characters?.forEach {
                it.name?.native?.let { title -> add(title) }
                it.name?.alternative?.let { titles -> addAll(titles.filterNotNull()) }
                it.name?.full?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findStaffNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindStaffName.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = GQL.query<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.staff?.forEach {
                it.name?.native?.let { title -> add(title) }
                it.name?.alternative?.let { titles -> addAll(titles.filterNotNull()) }
                it.name?.full?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findUserNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindUserName.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = GQL.query<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.users?.forEach {
                add(it.name)
            }
        }
    }

    override suspend fun findScoreByUsersAndMedias(
        userIds: List<Long>?,
        mediaIds: List<Long>?
    ): List<MediaList>? {
        val gqlQuery = findResourceAsText("/gql/FindScoreByMediaIdAndUserId.graphql")
        val variables = FindScore(userIds, mediaIds)
        val result = GQL.query<FindScore, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.mediaList
    }

    override suspend fun findCharacter(query: String?): List<Character>? {
        val gqlQuery = findResourceAsText("/gql/FindCharacter.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = GQL.query<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.characters
    }

    override suspend fun findStaff(query: String?): List<Staff>? {
        val gqlQuery = findResourceAsText("/gql/FindStaff.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = GQL.query<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.staff
    }

    override suspend fun findAiringMedia(mediaIds: List<Long>): List<AiringSchedule>? {
        val gqlQuery = findResourceAsText("/gql/FindAiringMedia.graphql")
        val variables = FindAiringMedia(mediaIds, page = 1, perPage = 50)
        val result = GQL.query<FindAiringMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.airingSchedules
    }

    @Serializable
    data class FindAiringMedia(
        val mediaIn: List<Long>,
        val page: Int,
        val perPage: Int
    )

    @Serializable
    data class FindUser(
        val name: String? = null
    )

    @Serializable
    data class Find(
        val query: String? = null,
        val page: Int,
        val perPage: Int
    )

    @Serializable
    data class FindMedia(
        val query: String? = null,
        val type: MediaType? = null,
        val page: Int,
        val perPage: Int,
        val sort: List<MediaSort>? = null,
        val formatIn: List<MediaFormat>? = null,
        val season: MediaSeason? = null,
        val seasonYear: Int? = null,
        val genreNotIn: List<String>? = null
    )

    @Serializable
    data class FindScore(
        val userId: List<Long>?,
        val mediaId: List<Long>?,
    )

    @Serializable
    data class UserResult(
        val User: User?
    )

    @Serializable
    data class PageResult(
        val Page: Page?
    )

}