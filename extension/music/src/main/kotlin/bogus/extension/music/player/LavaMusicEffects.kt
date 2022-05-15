package bogus.extension.music.player

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer

class LavaMusicEffects(private val player: AudioPlayer) : MusicEffects {
    private val equalizerBands = FloatArray(15)
    private var equalizer = EqualizerType.NONE
    private val filters = mutableListOf<Filter>()
    private var nightcoreRate = 0
    private var _volume = 0f
    override val volume: Int
        get() = player.volume

    private fun bandGain(bandGain: Int, gain: Float) {
        equalizerBands[bandGain] = gain
    }

    override suspend fun applyFilters() {
        player.setFilterFactory { _, format, output ->
            var filter: FloatPcmAudioFilter = output
            val filterList = mutableListOf<AudioFilter>()

            if (filters.contains(Filter.NIGHTCORE) || filters.contains(Filter.VAPORWAVE)) {
                val timescale = TimescalePcmAudioFilter(filter, format.channelCount, format.sampleRate)

                if (filters.contains(Filter.NIGHTCORE)) {
                    timescale.rate = nightcoreRate / 100.0
                }

                if (filters.contains(Filter.VAPORWAVE)) {
                    timescale.setSpeed(0.5).setPitchSemiTones(-7.0)
                } else {
                    timescale.setSpeed(1.0).setPitchSemiTones(0.0)
                }

                filterList.add(timescale)
                filter = timescale
            }

            if (filters.contains(Filter.KARAOKE)) {
                val karaokeFilter = KaraokePcmAudioFilter(filter, format.channelCount, format.sampleRate)
                    .setLevel(1.0f)
                    .setFilterBand(220f)
                    .setFilterWidth(100f)

                filterList.add(karaokeFilter)
                filter = karaokeFilter
            }

            if (equalizer != EqualizerType.NONE) {
                when (equalizer) {
                    EqualizerType.BASS_BOOST -> applyBassBoostEqualizer()
                    EqualizerType.POP -> applyPopEqualizer()
                    EqualizerType.ROCK -> applyRockEqualizer()
                    else -> {}
                }

                val equalizer = Equalizer(format.channelCount, filter, equalizerBands)

                filterList.add(equalizer)
                filter = equalizer
            }

            val volume = VolumePcmAudioFilter(filter).setVolume(_volume)
            filterList.add(volume)

            filterList.reversed()
        }
    }

    override suspend fun clearEqualizer() {
        equalizer = EqualizerType.NONE
    }

    override suspend fun clearFilter() {
        player.setFilterFactory { _, _, output ->
            listOf(
                VolumePcmAudioFilter(output).setVolume(_volume)
            )
        }
    }

    override suspend fun applyVolume(value: Int) {
        _volume = value / 100f
    }

    override suspend fun applyNightcore(value: Int) {
        nightcoreRate = value
        filters.add(Filter.NIGHTCORE)
    }

    override suspend fun applyVaporwave() {
        filters.add(Filter.VAPORWAVE)
    }

    override suspend fun applyKaraoke() {
        filters.add(Filter.KARAOKE)
    }

    override suspend fun applyEqualizer(effect: EqualizerType) {
        equalizer = effect
        applyFilters()
    }

    private fun applyPopEqualizer() {
        bandGain(0, -0.25f)
        bandGain(1, 0.48f)
        bandGain(2, 0.59f)
        bandGain(3, 0.72f)
        bandGain(4, 0.56f)
        bandGain(5, 0.15f)
        bandGain(6, -0.24f)
        bandGain(7, -0.24f)
        bandGain(8, -0.16f)
        bandGain(9, -0.16f)
        bandGain(10, 0f)
        bandGain(11, 0f)
        bandGain(12, 0f)
        bandGain(13, 0f)
        bandGain(14, 0f)
    }

    private fun applyRockEqualizer() {
        bandGain(0, 0.300f)
        bandGain(1, 0.250f)
        bandGain(2, 0.200f)
        bandGain(3, 0.100f)
        bandGain(4, 0.050f)
        bandGain(5, -0.050f)
        bandGain(6, -0.150f)
        bandGain(7, -0.200f)
        bandGain(8, -0.100f)
        bandGain(9, -0.050f)
        bandGain(10, 0.050f)
        bandGain(11, 0.100f)
        bandGain(12, 0.200f)
        bandGain(13, 0.250f)
        bandGain(14, 0.300f)
    }

    private fun applyBassBoostEqualizer() {
        bandGain(0, 0.6f)
        bandGain(1, 0.67f)
        bandGain(2, 0.67f)
        bandGain(3, 0f)
        bandGain(4, -0.15f)
        bandGain(5, 0.15f)
        bandGain(6, -0.25f)
        bandGain(7, 0.23f)
        bandGain(8, 0.35f)
        bandGain(9, 0.45f)
        bandGain(10, 0.55f)
        bandGain(11, 0.6f)
        bandGain(12, 0.55f)
        bandGain(13, 0f)
    }
}