package bogus.extension.anilist.embed

import bogus.extension.anilist.AniListExtension.Companion.EMBED_COLOR
import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.Character
import bogus.extension.anilist.weirdHtmlClean
import bogus.util.abbreviate
import bogus.util.appendIfNotMax
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

fun Character.createEmbed(): EmbedBuilder.() -> Unit = {
    val animeAppearance = StringBuilder()
    val mangaAppearance = StringBuilder()

    val mediaNodes = media?.nodes
    val mediaEdges = media?.edges

    if (mediaNodes != null && mediaEdges != null) {
        mediaNodes.zip(mediaEdges).forEach { pair ->
            val (media, edge) = pair
            // Ensure not null
            if (media != null && edge != null) {
                val mediaTitle = media.title?.english ?: media.title?.romaji
                val appearance = "- [${mediaTitle}](${media.siteUrl}) [${edge.characterRole?.displayName}]\n"

                when (media.type) {
                    bogus.extension.anilist.model.MediaType.ANIME -> {
                        animeAppearance.appendIfNotMax(appearance, EmbedBuilder.Field.Limits.name)
                    }
                    bogus.extension.anilist.model.MediaType.MANGA -> {
                        mangaAppearance.appendIfNotMax(appearance, EmbedBuilder.Field.Limits.name)
                    }
                    // Do nothing
                    else -> {}
                }
            }
        }
    }

    // Weirdly enough, duplicate names exists.
    val aliases = buildString {
        name?.alternative
            ?.filterNotNull()
            ?.filter { it.isNotEmpty() }
            ?.distinctBy { it }
            ?.forEach {
                appendIfNotMax("- ${it.trimEnd()}\n", EmbedBuilder.Field.Limits.name)
            }
    }

    title = buildString {
        val native = name?.native
        append(name?.full)
        if (native != null) {
            append(" (${native})")
        }
    }
    // Remove spoilers, fix new lines, clean up html bullshit
    description = this@createEmbed.description
        .replace(Regex("(?s)~!.*?!~"), "")
        .replace("\n\n\n", "\n")
        .aniClean()
        .weirdHtmlClean()
        .abbreviate(EmbedBuilder.Limits.description)
        .dropLastWhile { it != '\n' }

    thumbnail {
        url = this@createEmbed.image?.large ?: ""
    }

    url = siteUrl
    color = Color(EMBED_COLOR)

    if (animeAppearance.isNotBlank()) {
        field {
            name = "Anime Appearances"
            value = animeAppearance
                .toString()
                .abbreviate(EmbedBuilder.Field.Limits.name)
                .dropLastWhile { it != '\n' }
            inline = false
        }
    }

    if (mangaAppearance.isNotBlank()) {
        field {
            name = "Manga Appearances"
            value = mangaAppearance
                .toString()
                .abbreviate(EmbedBuilder.Field.Limits.name)
                .dropLastWhile { it != '\n' }
            inline = false
        }
    }

    if (aliases.isNotBlank()) {
        field {
            name = "Aliases"
            value = aliases
                .abbreviate(EmbedBuilder.Field.Limits.name)
            inline = false
        }
    }

    field {
        name = "Favorites"
        value = favourites.toString()
        inline = true
    }
}
