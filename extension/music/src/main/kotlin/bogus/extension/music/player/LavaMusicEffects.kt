package bogus.extension.music.player

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer

class LavaMusicEffects(private val player: AudioPlayer) : MusicEffects {
    override val volume: Int
        get() = player.volume

    override suspend fun applyFilters() {

    }

    override suspend fun clearEqualizer() {

    }

    override suspend fun clearFilter() {

    }

    override suspend fun applyVolume(value: Int) {
        player.volume = value
    }

    override suspend fun applyNightcore(value: Int) {

    }

    override suspend fun applyVaporwave() {

    }

    override suspend fun applyKaraoke() {

    }

    override suspend fun applyEqualizer(effect: Equalizer) {

    }
}