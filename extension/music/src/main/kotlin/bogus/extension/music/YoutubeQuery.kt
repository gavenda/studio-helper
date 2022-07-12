package bogus.extension.music

import bogus.util.asFMTLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

object YT {
    /**
     * https://regex101.com/r/Jg94Ag/1
     */
    private val responsePattern = Regex("""\["(.+?(?="))".+?(?=]])]]""")
    private val youtubeEndpoint = Url("https://suggestqueries-clients6.youtube.com/complete/search?client=youtube")
    private val client = HttpClient()
    private val logger = KotlinLogging.logger { }.asFMTLogger()

    suspend fun query(query: String): List<String> {
        val response = client.get(youtubeEndpoint) {
            url {
                parameter("q", query)
                parameter("cp", 10) // search in music category
            }
        }

        val responseList = responsePattern.findAll(response.bodyAsText()).map { it.groupValues[1] }.toList()

        logger.debug {
            message = "Youtube query"
            context = mapOf("response" to responseList)
        }

        return responseList
    }

    fun thumbnail(videoId: String): String {
        return "https://img.youtube.com/vi/${videoId}/maxresdefault.jpg"
    }

}
