package bogus.extension.music

import bogus.extension.music.spotify.*
import bogus.util.isUrl
import de.sonallux.spotify.api.SpotifyWebApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.streams.asSequence

object IdentifierParser : KoinComponent {

    private val musicDirectory: Path = Paths.get("", MUSIC_DIRECTORY).toAbsolutePath()

    data class IdentifierParseResult(
        val identifiers: List<String>,
        val local: Boolean = false,
        val spotify: Boolean = false
    )

    fun listFiles(): Sequence<String> {
        return Files.walk(musicDirectory)
            .asSequence()
            .filter { Files.isRegularFile(it) }
            .map { it.name }
    }

    fun fromList(list: List<String>): IdentifierParseResult {
        return IdentifierParseResult(
            identifiers = list
        )
    }

    private suspend fun findFile(fileName: String): List<String> {
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

    suspend fun toIdentifiers(song: String): IdentifierParseResult {
        if (song.startsWith("local:")) {
            return IdentifierParseResult(
                identifiers = findFile(song.split(":")[1]),
                local = true
            )
        }
        if (song.isUrl.not()) {
            return fromList( listOf("ytsearch:$song"))
        }

        val spotifyUri = parseSpotifyUri(song) ?: return IdentifierParseResult(
            identifiers = listOf(song)
        )
        val spotifyWebApi by inject<SpotifyWebApi>()

        when (spotifyUri) {
            is Track -> {
                val result = spotifyWebApi.findTrack(spotifyUri.id)
                val artist = result.artists.first().name
                return IdentifierParseResult(
                    identifiers = listOf("ytsearch:${result.name} - $artist"),
                    spotify = true
                )
            }
            is Album -> {
                val tracks = spotifyWebApi.findAllTracksInAlbum(spotifyUri.id)
                return IdentifierParseResult(
                    identifiers = tracks.map {
                        val artist = it.artists.first().name
                        return@map "ytsearch:${it.name} - $artist"
                    },
                    spotify = true
                )
            }
            is Playlist -> {
                val tracks = spotifyWebApi.findAllTracksInPlaylist(spotifyUri.id)
                return IdentifierParseResult(
                    identifiers = tracks.map {
                        val track = it.track as de.sonallux.spotify.api.models.Track
                        val artist = track.artists.first().name

                        return@map "ytsearch:${track.name} - $artist"
                    },
                    spotify = true
                )
            }
        }

        return fromList(emptyList())
    }

}