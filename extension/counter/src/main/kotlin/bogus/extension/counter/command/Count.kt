package bogus.extension.counter.command

import bogus.constants.AUTOCOMPLETE_ITEMS_LIMIT
import bogus.constants.ITEMS_PER_CHUNK
import bogus.extension.counter.CounterExtension
import bogus.extension.counter.CounterExtension.Companion.EMBED_COLOR
import bogus.extension.counter.DATE_FORMATTER
import bogus.extension.counter.PAGINATOR_TIMEOUT
import bogus.extension.counter.db.DbGuildCount
import bogus.extension.counter.db.counts
import bogus.paginator.respondingStandardPaginator
import bogus.util.idLong
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.common.Color
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.support.postgresql.ilike
import java.time.Instant

suspend fun CounterExtension.count() {
    publicSlashCommand {
        name = "count"
        locking = true
        description = "count.description"

        increase()
        list()
    }
}

private suspend fun PublicSlashCommand<*>.increase() = publicSubCommand(::CounterArgs) {
    val db by inject<Database>()

    name = "increase"
    description = "count.increase.description"

    check {
        anyGuild()
    }

    action {
        val guild = guild?.asGuildOrNull() ?: return@action
        val dbGuildCount = db.counts.firstOrNull {
            (it.discordGuildId eq guild.idLong) and (it.countName ilike arguments.counter)
        } ?: DbGuildCount {
            discordGuildId = guild.idLong
            countName = arguments.counter.capitalizeWords()
            countAmount = 0
            lastTimestamp = Instant.now()
            lastUserId = user.idLong
        }.apply {
            db.counts.add(this)
        }

        dbGuildCount.lastTimestamp = Instant.now()
        dbGuildCount.lastUserId = user.idLong
        dbGuildCount.countAmount = dbGuildCount.countAmount + 1
        dbGuildCount.flushChanges()

        respond {
            embed {
                color = Color(EMBED_COLOR)
                author {
                    name = "Count Increased"
                }
                description = """
                    :chart_with_upwards_trend: **${dbGuildCount.countName}** has been increased to **${dbGuildCount.countAmount}**!
                """.trimIndent()
                footer {
                    text = "Note: You can list the current count by executing /count list"
                }
            }
        }
    }
}

private suspend fun PublicSlashCommand<*>.list() = publicSubCommand {
    val db by inject<Database>()

    name = "list"
    locking = true
    description = "count.list.description"

    action {
        val guild = guild?.fetchGuildOrNull() ?: return@action
        val counters = db.counts.filter {
            it.discordGuildId eq guild.idLong
        }

        if (counters.isEmpty()) {
            respond {
                embed {
                    color = Color(EMBED_COLOR)
                    author {
                        name = "Counter Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    description = """
                        Counter(s) for **${guild.name}**
                        
                        __(there is no counter available)__
                    """.trimIndent()
                    footer {
                        text = "Note: Lacking count? Increase it now using /count increase"
                    }
                }
            }
            return@action
        }

        val paginator = respondingStandardPaginator {
            timeoutSeconds = PAGINATOR_TIMEOUT
            val chunked = counters.asKotlinSequence().chunked(ITEMS_PER_CHUNK)

            chunked.forEach { sequenceChunked ->
                page {
                    color = Color(EMBED_COLOR)
                    author {
                        name = "Counter Information"
                        icon = guild.getIconUrl(Image.Format.WEBP)
                    }
                    description = ":chart_with_upwards_trend: List of counters for **${guild.name}**"
                    sequenceChunked.forEach { counter ->
                        val dateFormatted = DATE_FORMATTER.format(counter.lastTimestamp)

                        field {
                            name = counter.countName
                            value = """
                                - Count: **${counter.countAmount}**
                                - Last Count: **$dateFormatted**
                                - By: <@${counter.lastUserId}>
                            """.trimIndent()
                        }
                    }
                    footer {
                        text = "Note: Lacking count? Increase it now using /count increase"
                    }
                }
            }
        }

        paginator.send()
    }
}

private class CounterArgs : KordExKoinComponent, Arguments() {
    val db by inject<Database>()

    val counter by string {
        name = "counter"
        description = "The name of the counter you want to increment."

        autoComplete {
            suggestString {
                val guildIdSnowflake = data.guildId.value ?: return@suggestString
                val guildIdLong = guildIdSnowflake.value.toLong()

                db.counts.filter {
                    (it.discordGuildId eq guildIdLong) and (it.countName ilike "${focusedOption.value}%")
                }
                    .take(AUTOCOMPLETE_ITEMS_LIMIT)
                    .forEach {
                        choice(it.countName, it.countName)
                    }
            }
        }
    }
}