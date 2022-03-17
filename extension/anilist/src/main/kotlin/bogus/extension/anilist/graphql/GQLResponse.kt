package bogus.extension.anilist.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GQLResponse<T>(
    val data: T
)
