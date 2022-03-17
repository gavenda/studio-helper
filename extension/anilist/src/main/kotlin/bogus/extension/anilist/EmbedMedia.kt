package bogus.extension.anilist

import bogus.extension.anilist.model.MediaListStatus

data class EmbedMedia(
    val discordName: String?,
    val status: MediaListStatus,
    val score: Float,
    val progress: Int
)
