package bogus.extension.anilist.graphql

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

@Suppress("BlockingMethodInNonBlockingContext")
suspend inline fun <reified V, reified R> gqlQuery(graphUrl: String, query: String, variables: V): R {
    val json by inject<Json>(Json::class.java)
    val client by inject<HttpClient>(HttpClient::class.java)
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
