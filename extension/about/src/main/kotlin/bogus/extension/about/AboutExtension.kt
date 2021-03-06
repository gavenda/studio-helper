package bogus.extension.about

import bogus.util.selfAvatarUrl
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AboutExtension : Extension() {
    override val name: String = "about"
    override val bundle: String = "about"

    private val formatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MMMM d, u HH:mm a O")
        .withZone(ZoneId.systemDefault())

    companion object {
        var EMBED_COLOR = 0
    }

    override suspend fun setup() {

        ephemeralSlashCommand {
            name = "about"
            description = "about.description"
            action {
                val java = System.getProperty("java.vendor")
                val javaVersion = System.getProperty("java.version")
                val sys = System.getProperty("os.name")
                val sysArch = System.getProperty("os.arch")
                val sysVersion = System.getProperty("os.version")
                val version = Properties().apply {
                    load(object {}.javaClass.getResourceAsStream("/version.properties"))
                }.getProperty("version") ?: "-"

                respond {
                    embed {
                        title = translate("about.embed.title")
                        url = translate("about.embed.url")
                        description = translate("about.embed.description")
                        color = Color(EMBED_COLOR)

                        thumbnail {
                            url = this@ephemeralSlashCommand.kord.selfAvatarUrl
                        }

                        field {
                            name = translate("about.embed.version")
                            value = version
                            inline = true
                        }
                        field {
                            name = translate("about.embed.language")
                            value = "[Kotlin](https://kotlinlang.org)"
                            inline = true
                        }
                        field {
                            name = translate("about.embed.framework")
                            value = "[Kord](https://github.com/kordlib/kord)"
                            inline = true
                        }
                        field {
                            name = translate("about.embed.operatingSystem")
                            value = "$java Java $javaVersion on $sys $sysVersion ($sysArch)"
                        }
                        field {
                            name = translate("about.embed.date-created")
                            value = formatter.format(this@ephemeralSlashCommand.kord.selfId.timestamp.toJavaInstant())
                        }
                        footer {
                            text = translate("about.embed.note")
                            icon = "https://github.com/fluidicon.png"
                        }
                    }
                }
            }
        }
    }
}