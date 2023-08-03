package bogus.extension.music.player

import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.schlaubi.lavakord.audio.player.*

class LinkMusicEffects(private val player: Player) : MusicEffects {
    private val filters = mutableListOf<Filter>()
    private var equalizer = EqualizerType.NONE
    private var nightcoreRate = 0
    private var _volume = 100

    override val volume get() = _volume
    override val activeEqualizer: String
        get() = if (equalizer != EqualizerType.NONE) {
            equalizer.name
                .lowercase()
                .replace("_", " ")
                .capitalizeWords()
        } else "-"
    override val activeFilters: String
        get() = filters.joinToString(prefix = " - ", postfix = "\n") {
            it.name
                .lowercase()
                .replace("_", " ")
                .capitalizeWords()
        }

    override suspend fun applyFilters() {
        player.applyFilters {
            volume = _volume / 100f

            if (filters.contains(Filter.NIGHTCORE)) {
                timescale {
                    rate = nightcoreRate / 100.0
                }
            }

            if (filters.contains(Filter.VAPORWAVE)) {
                timescale {
                    speed = 0.5
                }
                tremolo {
                    depth = 0.3f
                    frequency = 14f
                }
            }

            if (filters.contains(Filter.KARAOKE)) {
                karaoke {
                    level = 1.0f
                    monoLevel = 1.0f
                    filterBand = 220f
                    filterWidth = 100f
                }
            }

            when (equalizer) {
                EqualizerType.BASS_BOOST -> applyBassBoostEqualizer()
                EqualizerType.POP -> applyPopEqualizer()
                EqualizerType.ROCK -> applyRockEqualizer()
                EqualizerType.NONE -> reset()
            }
        }
    }

    override suspend fun clearEqualizer() {
        player.applyFilters { reset() }
        equalizer = EqualizerType.NONE
    }

    override suspend fun clearFilter() {
        player.resetFilters()
        filters.clear()
        applyFilters()
    }

    override suspend fun applyVolume(value: Int) {
        _volume = value
        applyFilters()
    }

    override suspend fun applyNightcore(value: Int) {
        nightcoreRate = value
        filters.add(Filter.NIGHTCORE)
        applyFilters()
    }

    override suspend fun applyVaporwave() {
        filters.add(Filter.VAPORWAVE)
        applyFilters()
    }

    override suspend fun applyKaraoke() {
        filters.add(Filter.KARAOKE)
        applyFilters()
    }

    override suspend fun applyEqualizer(effect: EqualizerType) {
        equalizer = effect
        applyFilters()
    }

    private fun Filters.applyPopEqualizer() {
        band(0) gain -0.25f
        band(1) gain 0.48f
        band(2) gain 0.59f
        band(3) gain 0.72f
        band(4) gain 0.56f
        band(5) gain 0.15f
        band(6) gain -0.24f
        band(7) gain -0.24f
        band(8) gain -0.16f
        band(9) gain -0.16f
    }

    private fun Filters.applyRockEqualizer() {
        band(0) gain 0.300f
        band(1) gain 0.250f
        band(2) gain 0.200f
        band(3) gain 0.100f
        band(4) gain 0.050f
        band(5) gain -0.050f
        band(6) gain -0.150f
        band(7) gain -0.200f
        band(8) gain -0.100f
        band(9) gain -0.050f
        band(10) gain 0.050f
        band(11) gain 0.100f
        band(12) gain 0.200f
        band(13) gain 0.250f
        band(14) gain 0.300f
    }

    private fun Filters.applyBassBoostEqualizer() {
        band(0) gain 0.25f
        band(1) gain 0.25f
        band(2) gain 0.125f
        band(3) gain 0.0625f
    }
}