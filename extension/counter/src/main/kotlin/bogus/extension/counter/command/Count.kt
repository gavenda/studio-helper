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
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.common.Color
import dev.kord.core.behavior.interaction.suggestString
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
        name = "command.count"
        locking = true
        description = "command.count.description"

        increase()
        list()
    }
}

private suspend fun PublicSlashCommand<*>.increase() = publicSubCommand(::CounterArgs) {
    val db by inject<Database>()

    name = "command.count.increase"
    description = "command.count.increase.description"

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
                title = translate("response.count.increase.author.name")
                description = translate(
                    "response.count.increase.description",
                    arrayOf(dbGuildCount.countName, dbGuildCount.countAmount)
                )
                footer {
                    text = translate("response.count.increase.footer.text")
                }
            }
        }
    }
}

private suspend fun PublicSlashCommand<*>.list() = publicSubCommand {
    val db by inject<Database>()

    name = "command.count.list"
    locking = true
    description = "command.count.list.description"

    action {
        val guild = guild?.asGuildOrNull() ?: return@action
        val counters = db.counts.filter {
            it.discordGuildId eq guild.idLong
        }

        if (counters.isEmpty()) {
            respond {
                embed {
                    color = Color(EMBED_COLOR)
                    title = translate("response.count.list.author.name")
                    description = translate("response.count.list.description.empty", arrayOf(guild.name))
                    footer {
                        text = translate("response.count.list.footer.text")
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
                    title = translate("response.count.list.author.name")
                    description = translate("response.count.list.description", arrayOf(guild.name))
                    sequenceChunked.forEach { counter ->
                        val dateFormatted = DATE_FORMATTER.format(counter.lastTimestamp)
                        val lastUserId = counter.lastUserId.toString()
                        field {
                            name = counter.countName
                            value = translate(
                                "response.count.list.field",
                                arrayOf(counter.countAmount, dateFormatted, lastUserId)
                            )
                        }
                    }
                    footer {
                        text = translate("response.count.list.footer.text")
                    }
                }
            }
        }

        paginator.send()
    }
}

private class CounterArgs : KordExKoinComponent, Arguments() {
    val db by inject<Database>()

    val counter by coalescingString {
        name = "command.count.increase.args.counter"
        description = "command.count.increase.args.counter.description"

        autoComplete {
            suggestString {
                val guildIdSnowflake = data.guildId.value ?: return@suggestString
                val guildIdLong = guildIdSnowflake.value.toLong()

                if (focusedOption.value.isNotBlank()) {
                    db.counts.filter {
                        (it.discordGuildId eq guildIdLong) and (it.countName ilike "${focusedOption.value}%")
                    }
                        .take(AUTOCOMPLETE_ITEMS_LIMIT)
                        .forEach {
                            choice(it.countName, it.countName)
                        }
                } else {
                    db.counts.filter {
                        (it.discordGuildId eq guildIdLong)
                    }
                        .take(AUTOCOMPLETE_ITEMS_LIMIT)
                        .forEach {
                            choice(it.countName, it.countName)
                        }
                }
            }
        }
    }
}