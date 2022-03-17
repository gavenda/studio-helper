package bogus.extension.anilist.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GQLRequest<T>(
    val query: String,
    val variables: T
)