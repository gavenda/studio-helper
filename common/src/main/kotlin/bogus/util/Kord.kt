package bogus.util

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User

val GuildBehavior.idLong get(): Long = id.value.toLong()
val Guild.idLong get(): Long = id.value.toLong()
val UserBehavior.idLong get(): Long = id.value.toLong()
val User.idLong get(): Long = id.value.toLong()
