package bogus.extension.anilist.graphql

import bogus.util.findResourceAsText
import kotlinx.serialization.Serializable

class AniListGraphQL : AniList {
    private val graphUri = "https://graphql.anilist.co"

    override suspend fun findUserByName(name: String): bogus.extension.anilist.model.User? {
        val gqlQuery = findResourceAsText("/gql/FindUserByName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findUserStatisticsByName(name: String): bogus.extension.anilist.model.User? {
        val gqlQuery = findResourceAsText("/gql/FindStatisticsByUserName.graphql")
        val variables = FindUser(name)
        val result = gqlQuery<FindUser, UserResult>(graphUri, gqlQuery, variables)
        return result.User
    }

    override suspend fun findMedia(
        query: String,
        type: bogus.extension.anilist.model.MediaType?,
        hentai: Boolean
    ): List<bogus.extension.anilist.model.Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMedia.graphql")
        val variables = FindMedia(
            query = query,
            type = type,
            page = 1,
            perPage = 10,
            genreNotIn = if (!hentai) listOf("Hentai") else null
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaByRanking(
        amount: Int,
        formatIn: List<bogus.extension.anilist.model.MediaFormat>?,
        season: bogus.extension.anilist.model.MediaSeason?,
        seasonYear: Int?,
        hentai: Boolean
    ): List<bogus.extension.anilist.model.Media>? {
        val gqlQuery = findResourceAsText("/gql/FindMediaByRanking.graphql")
        val variables = FindMedia(
            page = 1,
            perPage = amount,
            sort = listOf(bogus.extension.anilist.model.MediaSort.SCORE_DESC),
            formatIn = formatIn,
            season = season,
            seasonYear = seasonYear,
            genreNotIn = if (!hentai) listOf("Hentai") else null
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.media
    }

    override suspend fun findMediaTitles(query: String, type: bogus.extension.anilist.model.MediaType?): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindMediaName.graphql")
        val variables = FindMedia(
            query = query,
            type = type,
            page = 1,
            perPage = 10
        )
        val result = gqlQuery<FindMedia, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.media?.forEach {
                it.title?.native?.let { title -> add(title) }
                it.title?.romaji?.let { title -> add(title) }
            }
        }.distinct()
    }

    override suspend fun findCharacterNames(query: String): List<String> {
        val gqlQuery = findResourceAsText("/gql/FindCharacterName.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
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
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
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
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return buildList {
            result.Page?.users?.forEach {
                add(it.name)
            }
        }
    }

    override suspend fun findScoreByUsersAndMedias(
        userIds: List<Long>?,
        mediaIds: List<Long>?
    ): List<bogus.extension.anilist.model.MediaList>? {
        val gqlQuery = findResourceAsText("/gql/FindScoreByMediaIdAndUserId.graphql")
        val variables = FindScore(userIds, mediaIds)
        val result = gqlQuery<FindScore, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.mediaList
    }

    override suspend fun findCharacter(query: String?): List<bogus.extension.anilist.model.Character>? {
        val gqlQuery = findResourceAsText("/gql/FindCharacter.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.characters
    }

    override suspend fun findStaff(query: String?): List<bogus.extension.anilist.model.Staff>? {
        val gqlQuery = findResourceAsText("/gql/FindStaff.graphql")
        val variables = Find(query, page = 1, perPage = 10)
        val result = gqlQuery<Find, PageResult>(graphUri, gqlQuery, variables)
        return result.Page?.staff
    }

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
        val type: bogus.extension.anilist.model.MediaType? = null,
        val page: Int,
        val perPage: Int,
        val sort: List<bogus.extension.anilist.model.MediaSort>? = null,
        val formatIn: List<bogus.extension.anilist.model.MediaFormat>? = null,
        val season: bogus.extension.anilist.model.MediaSeason? = null,
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
        val User: bogus.extension.anilist.model.User?
    )

    @Serializable
    data class PageResult(
        val Page: bogus.extension.anilist.model.Page?
    )

}