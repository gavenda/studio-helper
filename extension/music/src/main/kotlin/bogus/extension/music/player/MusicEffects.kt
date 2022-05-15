package bogus.extension.music.player

interface MusicEffects {
    val volume: Int

    suspend fun applyFilters()

    suspend fun clearEqualizer()

    suspend fun clearFilter()

    suspend fun applyVolume(value: Int)

    suspend fun applyNightcore(value: Int)

    suspend fun applyVaporwave()

    suspend fun applyKaraoke()

    suspend fun applyEqualizer(effect: Equalizer)
}

enum class Equalizer {
    BASS_BOOST,
    POP,
    ROCK,
    NONE
}

enum class Filter {
    KARAOKE,
    VAPORWAVE,
    NIGHTCORE
}