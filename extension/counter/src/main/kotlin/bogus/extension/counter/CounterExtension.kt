package bogus.extension.counter

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.constants.ITEMS_PER_CHUNK
import bogus.paginator.respondingStandardPaginator
import bogus.util.asLogFMT
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class CounterExtension : Extension() {
    override val name = "count"

    private val path = Paths.get("", "db").toAbsolutePath()
    private val pathFile = path.resolve("counter.json")
    private val counters = mutableMapOf<String, Long>()
    private val log = KotlinLogging.logger { }.asLogFMT()

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                withContext(Dispatchers.IO) {
                    try {
                        if (!path.exists()) {
                            Files.createDirectories(path)
                        }
                        if (!pathFile.exists()) {
                            Files.createFile(pathFile)
                        }

                        val jsonString = Files.readString(pathFile, Charsets.UTF_8)

                        if (jsonString.isNotBlank()) {
                            val decodedMap = Json.decodeFromString<Map<String, Long>>(jsonString)
                            counters.putAll(decodedMap)
                        }
                    } catch (ex: Exception) {
                        log.error(ex, ex.message)
                    }
                }
            }
        }

        publicSlashCommand {
            name = "count"
            locking = true
            description = "Counter related group."

            publicSubCommand(::CounterArgs) {
                name = "increment"
                description = "Increment a counter."

                action {
                    val existing = counters[arguments.counter] ?: 0
                    val count = existing + 1

                    counters[arguments.counter] = count

                    respond {
                        content = """${arguments.counter} - **${count}**"""
                    }

                    val json = Json.encodeToString(counters)

                    withContext(Dispatchers.IO) {
                        Files.newBufferedWriter(pathFile, Charsets.UTF_8)
                    }.use { it.write(json) }
                }
            }

            publicSubCommand {
                name = "list"
                locking = true
                description = "List the counters (global)."

                action {
                    if (counters.isEmpty()) {
                        respond {
                            embed {
                                title = "Counter List (global)"
                                description = "(no counters available)"
                            }
                        }
                        return@action
                    }

                    val paginator = respondingStandardPaginator {
                        timeoutSeconds = PAGINATOR_TIMEOUT
                        val chunked = counters.asSequence().chunked(ITEMS_PER_CHUNK)

                        chunked.forEach { sequenceChunked ->
                            page {
                                title = "Counter List (global)"
                                description = buildString {
                                    sequenceChunked.forEach { counter ->
                                        append("`${counter.key}` - **${counter.value}**\n")
                                    }
                                }
                            }
                        }
                    }

                    paginator.send()
                }
            }
        }
    }

    inner class CounterArgs : Arguments() {
        val counter by string {
            name = "counter"
            description = "The name of the counter you want to increment."

            autoComplete {
                suggestString {
                    counters.keys
                        .filter { it.contains(focusedOption.value, true) }
                        .take(AUTOCOMPLETE_ITEMS_LIMIT)
                        .forEach {
                            choice(it, it)
                        }
                }
            }
        }
    }
}
