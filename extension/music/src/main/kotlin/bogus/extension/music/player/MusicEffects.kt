package bogus.extension.music.player

interface MusicEffects {
    val volume: Int
    val activeEqualizer: String
    val activeFilters: String

    /**
     * Apply filters.
     */
    suspend fun applyFilters()

    /**
     * Clear equalizer.
     */
    suspend fun clearEqualizer()

    /**
     * Clear filters.
     */
    suspend fun clearFilter()

    /**
     * Apply volume.
     */
    suspend fun applyVolume(value: Int)

    /**
     * Apply nightcore.
     */
    suspend fun applyNightcore(value: Int)

    /**
     * Apply vaporwave effect.
     */
    suspend fun applyVaporwave()

    /**
     * Apply karaoke effect.
     */
    suspend fun applyKaraoke()

    /**
     * Apply equalizer.
     */
    suspend fun applyEqualizer(effect: EqualizerType)
}

enum class EqualizerType {
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