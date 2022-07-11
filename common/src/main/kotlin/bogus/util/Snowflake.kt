package bogus.util

import dev.kord.common.entity.Snowflake

val Long.asSnowflake get() = Snowflake(this)