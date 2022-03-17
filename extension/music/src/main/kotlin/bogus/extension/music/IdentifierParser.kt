package bogus.extension.music

import bogus.extension.music.spotify.*
import bogus.util.isUrl
import de.sonallux.spotify.api.SpotifyWebApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.streams.asSequence

object IdentifierParser : KoinComponent {

    private suspend fun findFile(fileName: String): List<String> {
        val musicDirectory = Paths.get("", MUSIC_DIRECTORY).toAbsolutePath()
        val musicFile = withContext(Dispatchers.IO) {
            Files.walk(musicDirectory)
                .asSequence()
                .filter { it.name.lowercase().startsWith(fileName.lowercase()) }
                .firstOrNull()
        }
        val musicFilePath = musicDirectory.resolve(fileName)
        val file = musicFile ?: musicFilePath
        return listOf(file.toAbsolutePath().toString())
    }

    suspend fun toIdentifiers(song: String): List<String> {
        if (song.startsWith("local:")) {
            return findFile(song.split(":")[1])
        }
        if (song.isUrl.not()) {
            return listOf("ytsearch:$song")
        }

        val spotifyUri = parseSpotifyUri(song) ?: return listOf(song)
        val spotifyWebApi by inject<SpotifyWebApi>()

        when (spotifyUri) {
            is Track -> {
                val result = spotifyWebApi.findTrack(spotifyUri.id)
                val artist = result.artists.first().name
                return listOf("ytsearch:${result.name} - $artist")
            }
            is Album -> {
                val tracks = spotifyWebApi.findAllTracksInAlbum(spotifyUri.id)
                return tracks.map {
                    val artist = it.artists.first().name

                    return@map "ytsearch:${it.name} - $artist"
                }
            }
            is Playlist -> {
                val tracks = spotifyWebApi.findAllTracksInPlaylist(spotifyUri.id)
                return tracks.map {
                    val track = it.track as de.sonallux.spotify.api.models.Track
                    val artist = track.artists.first().name

                    return@map "ytsearch:${track.name} - $artist"
                }
            }
        }

        return listOf()
    }

}