package bogus.util

import dev.kord.core.entity.Entity

val Entity.idLong get(): Long = id.value.toLong()
