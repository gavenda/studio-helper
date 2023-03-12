package bogus.extension.music.spotify

import bogus.extension.music.SPOTIFY_ENABLED
import bogus.util.urlDecode
import de.sonallux.spotify.api.SpotifyWebApi
import de.sonallux.spotify.api.models.PlaylistTrack
import de.sonallux.spotify.api.models.SimplifiedTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

fun parseQueryString(input: String): MutableMap<String, String> {
    val data = mutableMapOf<String, String>()
    val arrParameters = input.split("&")
    for (tempParameterString in arrParameters) {
        val arrTempParameter = tempParameterString.split("=")
        if (arrTempParameter.size >= 2) {
            val parameterKey = arrTempParameter[0]
            val parameterValue = arrTempParameter[1]
            data[parameterKey] = parameterValue
        } else {
            val parameterKey = arrTempParameter[0]
            data[parameterKey] = ""
        }
    }
    return data
}

/**
 * Parse a spotify uri. Returns null if spotify is not enabled or if it cannot determine the type.
 * @param uri the uri string to parse
 * @return the [SpotifyURI] object, use type instance to determine.
 */
fun parseSpotifyUri(uri: String): SpotifyURI? {
    if (!SPOTIFY_ENABLED) return null

    val url = URL(uri)
    val protocol = url.protocol
    val hostname = url.host
    val pathname = url.path
    val query = url.query

    if (hostname == "embed.spotify.com" || hostname == "open.spotify.com") {
        val parsedQueryString = parseQueryString(query)
        return parseSpotifyUri(parsedQueryString.getOrDefault("uri", ""))
    }

    if (protocol == "spotify:") {
        val parts = uri.split(":")
        return parseParts(uri, parts)
    }

    if (pathname == null) {
        throw IllegalArgumentException("No pathname")
    }

    // `http:` or `https:`
    // val parts = pathname.split('/')
    return null
}

fun parseParts(uri: String, inputParts: List<String>): SpotifyURI? {
    var parts = inputParts
    val len = parts.size
    if (parts[1] == "embed") {
        parts = parts.slice(0..1)
    }
    if (parts[1] == "search") {
        return Search(uri, parts.slice(0..2).joinToString(":").urlDecode())
    }
    if (len >= 3 && parts[1] === "local") {
        return Local(
            uri,
            parts[2].urlDecode(),
            parts[3].urlDecode(),
            parts[4].urlDecode(),
            parts[5].toInt()
        )
    }
    if (len == 3 && parts[1] == "playlist") {
        return Playlist(uri, parts[2].urlDecode())
    }
    if (len == 3 && parts[1] == "user") {
        return User(uri, parts[2].urlDecode())
    }
    if (len >= 5) {
        return Playlist(uri, parts[4].urlDecode(), parts[2].urlDecode())
    }
    if (len >= 4 && parts[3] == "starred") {
        return Playlist(uri, "starred", parts[2].urlDecode())
    }
    if (parts[1] == "artist") {
        return Artist(uri, parts[2])
    }
    if (parts[1] == "album") {
        return Album(uri, parts[2])
    }
    if (parts[1] == "track") {
        return Track(uri, parts[2])
    }
    if (parts[1] == "episode") {
        return Episode(uri, parts[2])
    }
    if (parts[1] == "show") {
        return Show(uri, parts[2])
    }

    return null
}

suspend fun SpotifyWebApi.findTrack(trackId: String): de.sonallux.spotify.api.models.Track {
    val request = tracksApi
        .getTrack(trackId)
        .build()
    return withContext(Dispatchers.IO) {
        request.execute()
    }
}

suspend fun SpotifyWebApi.findAllTracksInAlbum(albumId: String): List<SimplifiedTrack> {
    return findAllTracksInAlbum(listOf(), 0, albumId)
}

private suspend fun SpotifyWebApi.findAllTracksInAlbum(
    items: List<SimplifiedTrack>,
    offset: Int,
    albumId: String
): List<SimplifiedTrack> {
    val request = albumsApi.getAlbumsTracks(albumId)
        .offset(offset)
        .build()
    val result = withContext(Dispatchers.IO) { request.execute() }
    val sum = result.offset + result.limit
    if (sum < result.total) {
        return findAllTracksInAlbum(items.plus(result.items), sum, albumId)
    }
    return items.plus(result.items)
}

suspend fun SpotifyWebApi.findAllTracksInPlaylist(playlistId: String): List<PlaylistTrack> {
    return findAllTracksInPlaylist(emptyList(), 0, playlistId)
}

private suspend fun SpotifyWebApi.findAllTracksInPlaylist(
    items: List<PlaylistTrack>,
    offset: Int,
    playlistId: String
): List<PlaylistTrack> {
    val request = playlistsApi.getPlaylistsTracks(playlistId)
        .offset(offset)
        .build()
    val result = withContext(Dispatchers.IO) { request.execute() }
    val sum = result.offset + result.limit
    if (sum < result.total) {
        return findAllTracksInPlaylist(items.plus(result.items), sum, playlistId)
    }
    return items.plus(result.items)
}
