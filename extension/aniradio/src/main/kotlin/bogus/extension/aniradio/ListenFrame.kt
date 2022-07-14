@file:UseSerializers(ListenOpSerializer::class)

package bogus.extension.aniradio

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class ListenOp {
    HEARTBEAT,
    PLAYBACK
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ListenOp::class)
object ListenOpSerializer : KSerializer<ListenOp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlinx.serialization.IntAsListenOpSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): ListenOp {
        return when(decoder.decodeInt()) {
            0 -> ListenOp.HEARTBEAT
            1 -> ListenOp.PLAYBACK
            else -> error("Unknown op code")
        }
    }

    override fun serialize(encoder: Encoder, value: ListenOp) {
        encoder.encodeInt(value.ordinal)
    }
}

@Serializable
data class ListenFrame(
    val op: ListenOp,
    @SerialName(value = "d")
    val data: ListenData? = null,
    @SerialName(value = "t")
    val type: String? = null
)

@Serializable
data class ListenData(
    val message: String? = null,
    val heartbeat: Long? = null,
    val song: ListenSong? = null,
    val listeners: Int? = null
)

@Serializable
data class ListenRequester(
    val name: String
)

@Serializable
data class ListenEvent(
    val name: String,
    val image: String?
)

@Serializable
data class ListenSong(
    val id: Int,
    val title: String,
    val artists: List<ListenInfo>,
    val albums: List<ListenInfo>,
    val duration: Int,
    val favorite: Boolean? = null,
    val requester: ListenRequester? = null,
    val event: ListenEvent? = null
) {
    companion object {
        val EMPTY = ListenSong(
            id = 0,
            title = "",
            artists = emptyList(),
            albums = emptyList(),
            duration = 0
        )
    }
}

@Serializable
data class ListenInfo(
    val id: Int,
    val name: String,
    val nameRomaji: String?,
    val image: String?
)

