package bogus.extension.anilist.graphql

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

object GQL : KordExKoinComponent {
    val json by inject<Json>()
    val client by inject<HttpClient>()

    suspend inline fun <reified V, reified R> query(graphUrl: String, query: String, variables: V): R {
        val gqlRequest = GQLRequest(query, variables)
        val requestBody = json.encodeToString(gqlRequest)

        val response = client.post(graphUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        val responseBody = response.bodyAsText()
        val gqlResponse = json.decodeFromString<GQLResponse<R>>(responseBody)

        return gqlResponse.data
    }
}

