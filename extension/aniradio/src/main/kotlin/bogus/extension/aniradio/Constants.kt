package bogus.extension.aniradio

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

const val FRAME_BUFFER_SIZE = 1024
const val EXTENSION_NAME = "aniradio"
const val JPOP_RADIO_URI = "https://listen.moe/m3u8/jpop.m3u"
const val JPOP_RADIO_GATEWAY = "wss://listen.moe/gateway_v2"
const val KPOP_RADIO_URI = "https://listen.moe/m3u8/kpop.m3u"
const val KPOP_RADIO_GATEWAY = "wss://listen.moe/kpop/gateway_v2"

enum class RadioType(override val readableName: String) : ChoiceEnum {
    JPOP("J-POP"),
    KPOP("K-POP")
}