package bogus.extension.music

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.core.entity.ReactionEmoji

internal val EmojiPlay = ReactionEmoji.Custom(
    id = Snowflake(904451990623514645),
    name = "play",
    isAnimated = false
)
internal val EmojiSkip = ReactionEmoji.Custom(
    id = Snowflake(904451990225051701),
    name = "skip",
    isAnimated = false
)
internal val EmojiPause = ReactionEmoji.Custom(
    id = Snowflake(904469954215157771),
    name = "pause",
    isAnimated = false
)
internal val EmojiShuffle = ReactionEmoji.Custom(
    id = Snowflake(904477192283631627),
    name = "shuffle",
    isAnimated = false
)
internal val EmojiNext = ReactionEmoji.Custom(
    id = Snowflake(905638284468830229),
    name = "next",
    isAnimated = false
)
internal val EmojiPrev = ReactionEmoji.Custom(
    id = Snowflake(905638284074557461),
    name = "previous",
    isAnimated = false
)
internal val EmojiVolumeUp = ReactionEmoji.Custom(
    id = Snowflake(954645250276737066),
    name = "volumeup",
    isAnimated = false
)
internal val EmojiVolumeDown = ReactionEmoji.Custom(
    id = Snowflake(954645250255753266),
    name = "volumedown",
    isAnimated = false
)
internal val EmojiRepeatAll = ReactionEmoji.Custom(
    id = Snowflake(954650890969702430),
    name = "repeatall",
    isAnimated = false
)
internal val EmojiRepeatAllOn = ReactionEmoji.Custom(
    id = Snowflake(954650890986459156),
    name = "repeatallon",
    isAnimated = false
)
internal val EmojiRepeatSingle = ReactionEmoji.Custom(
    id = Snowflake(954650890915184732),
    name = "repeatsingle",
    isAnimated = false
)
internal val EmojiRepeatSingleOn = ReactionEmoji.Custom(
    id = Snowflake(954650890927742996),
    name = "repeatsingleon",
    isAnimated = false
)
internal val EmojiStop = ReactionEmoji.Custom(
    id = Snowflake(904469668725669938),
    name = "stop",
    isAnimated = false
)
internal val EmojiMusicNote = DiscordPartialEmoji(
    id = Snowflake(954684659411849256),
    name = "musicnote",
    animated = OptionalBoolean.Value(false)
)
