package bogus.extension.anilist.embed

import bogus.extension.anilist.AniListExtension.Companion.EMBED_COLOR
import bogus.extension.anilist.aniClean
import bogus.extension.anilist.model.Staff
import bogus.extension.anilist.weirdHtmlClean
import bogus.util.abbreviate
import bogus.util.appendIfNotMax
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

fun Staff.createEmbed(): EmbedBuilder.() -> Unit = {
    val characterNodes = characters?.nodes
    val characterEdges = characters?.edges
    val staffMediaNodes = staffMedia?.nodes
    val staffMediaEdges = staffMedia?.edges

    val charactersVoiced = buildString {
        if (characterNodes != null && characterEdges != null) {
            characterNodes.zip(characterEdges).forEach { pair ->
                val (node, edge) = pair
                // Ensure not null
                if (node != null && edge != null) {
                    val title = node.name?.full ?: node.name?.native
                    val appearance = "- [${title}](${node.siteUrl}) [${edge.role?.displayName}]\n"

                    append(appearance)
                }
            }
        }
    }

    val workedOn = buildString {
        if (staffMediaNodes != null && staffMediaEdges != null) {
            staffMediaNodes.zip(staffMediaEdges).forEach { pair ->
                val (node, edge) = pair
                // Ensure not null
                if (node != null && edge != null) {
                    val title = node.title?.english ?: node.title?.romaji
                    val appearance = "- [${title}](${node.siteUrl}) [${edge.staffRole}]\n"

                    append(appearance)
                }
            }
        }
    }

    val aliases = buildString {
        // Weirdly enough, duplicate names exists.
        name?.alternative
            ?.filterNotNull()
            ?.filter { it.isNotEmpty() }
            ?.distinctBy { it }
            ?.forEach {
                appendIfNotMax("- ${it.trimEnd()}\n", EmbedBuilder.Field.Limits.name)
            }
    }

    // Remove spoilers, fix new lines, clean up html bullshit
    val resultDescription = this@createEmbed.description
        .replace(Regex("(?s)~!.*?!~"), "")
        .replace("\n\n\n", "\n")
        .aniClean()
        .weirdHtmlClean()
        .abbreviate(EmbedBuilder.Limits.description)
        .dropLastWhile { it != '\n' }

    // Staff title
    title = buildString {
        val native = name?.native
        append(name?.full)
        if (native != null) {
            append(" (${native})")
        }
    }

    author {
        name = "ID#$id"
    }

    description = resultDescription
    thumbnail {
        url = this@createEmbed.image?.large ?: ""
    }
    url = siteUrl
    color = Color(EMBED_COLOR)

    if (charactersVoiced.isNotBlank()) {
        field {
            name = "Characters Voiced"
            value = charactersVoiced
                .abbreviate(EmbedBuilder.Field.Limits.name)
                .dropLastWhile { it != '\n' }
            inline = false
        }
    }

    if (workedOn.isNotBlank()) {
        field {
            name = "Worked On"
            value = workedOn
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