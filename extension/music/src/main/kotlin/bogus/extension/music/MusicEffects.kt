package bogus.extension.music

import dev.schlaubi.lavakord.audio.player.*

class MusicEffects(private val player: Player) {

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

    private val filters = mutableListOf<Filter>()
    private var equalizer = Equalizer.NONE

    private var nightcoreRate = 0
    private var _volume = 100

    val volume get() = _volume

    suspend fun applyFilters() {
        player.applyFilters {
            volume = _volume / 100f
            filters.forEach { effect ->
                when (effect) {
                    Filter.NIGHTCORE -> {
                        timescale {
                            rate = nightcoreRate / 100f
                        }
                    }
                    Filter.VAPORWAVE -> {
                        timescale {
                            speed = 0.5f
                        }
                        tremolo {
                            depth = 0.3f
                            frequency = 14f
                        }
                    }
                    Filter.KARAOKE -> {
                        karaoke {
                            level = 1.0f
                            monoLevel = 1.0f
                            filterBand = 220f
                            filterWidth = 100f
                        }
                    }
                }
            }

            when (equalizer) {
                Equalizer.BASS_BOOST -> applyBassBoostEqualizer()
                Equalizer.POP -> applyPopEqualizer()
                Equalizer.ROCK -> applyRockEqualizer()
                Equalizer.NONE -> bands.clear()
            }
        }
    }

    suspend fun clearEqualizer() {
        player.applyFilters { bands.clear() }
        equalizer = Equalizer.NONE
    }

    suspend fun clearFilter() {
        player.resetFilters()
        filters.clear()
        applyFilters()
    }

    suspend fun applyVolume(value: Int) {
        _volume = value
        applyFilters()
    }

    suspend fun applyNightcore(value: Int) {
        nightcoreRate = value
        filters.add(Filter.NIGHTCORE)
        applyFilters()
    }

    suspend fun applyVaporwave() {
        filters.add(Filter.VAPORWAVE)
        applyFilters()
    }

    suspend fun applyKaraoke() {
        filters.add(Filter.KARAOKE)
        applyFilters()
    }

    suspend fun applyEqualizer(effect: Equalizer) {
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
        band(10) gain 0f
        band(11) gain 0f
        band(12) gain 0f
        band(13) gain 0f
        band(14) gain 0f
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
        band(0) gain 0.6f
        band(1) gain 0.67f
        band(2) gain 0.67f
        band(3) gain 0f
        band(4) gain -0.15f
        band(5) gain 0.15f
        band(6) gain -0.25f
        band(7) gain 0.23f
        band(8) gain 0.35f
        band(9) gain 0.45f
        band(10) gain 0.55f
        band(11) gain 0.6f
        band(12) gain 0.55f
        band(13) gain 0f
    }
}