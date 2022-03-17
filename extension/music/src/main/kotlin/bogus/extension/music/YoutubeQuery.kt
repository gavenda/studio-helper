package bogus.extension.music

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import mu.KotlinLogging

// https://regex101.com/r/Jg94Ag/1
private val responsePattern = Regex("""\["(.+?(?="))".+?(?=]])]]""")
private val youtubeEndpoint = Url("https://suggestqueries-clients6.youtube.com/complete/search?client=youtube")
private val client = HttpClient()
private val logger = KotlinLogging.logger { }

internal suspend fun youtubeQuery(query: String): List<String> {
    val response = client.get<String>(youtubeEndpoint) {
        url {
            parameter("q", query)
            parameter("cp", 10) // search in music category
        }
    }

    val responseList = responsePattern.findAll(response).map { it.groupValues[1] }.toList()

    logger.info { "response=$responseList" }

    return responseList
}