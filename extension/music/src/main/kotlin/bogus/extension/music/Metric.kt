package bogus.extension.music

object Metric {
    // Counters
    const val SONGS_PLAYED = "songs.played"
    const val SONGS_QUEUED = "songs.queued"
    const val SONGS_STOPPED = "songs.stopped"
    const val SONGS_REMOVED = "songs.removed"

    // Gauge
    const val SONGS_PLAYING = "songs.playing"

    object Tag {
        const val COMMAND = "command"
    }
}