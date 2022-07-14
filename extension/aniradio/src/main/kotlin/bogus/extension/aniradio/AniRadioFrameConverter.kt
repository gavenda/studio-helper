package bogus.extension.aniradio

import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AniRadioFrameConverter : WebsocketContentConverter {
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: Frame): Any {
        val frame = content as Frame.Text
        return json.decodeFromString<ListenFrame>(frame.readText())
    }

    override fun isApplicable(frame: Frame): Boolean {
        return frame is Frame.Text
    }

    override suspend fun serialize(charset: Charset, typeInfo: TypeInfo, value: Any): Frame {
        val frame = value as ListenFrame
        return Frame.Text(json.encodeToString(frame))
    }
}