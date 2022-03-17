package bogus.util

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Abbreviates this string based on the specified maximum.
 * @max the maximum length before applying abbreviation
 */
fun String.abbreviate(max: Int): String {
    if (length <= max) return this
    return take(max).substring(0, max - 3) + Typography.ellipsis
}

/**
 * Returns true if the string is a url.
 */
val String.isUrl: Boolean
    get() {
        return this.startsWith("http://") || this.startsWith("https://")
    }

/**
 * Return a url-decoded string.
 */
fun String.urlDecode(): String {
    return URLDecoder.decode(this.replace("+", " "), Charsets.UTF_8.name())
}

/**
 * Return a url-encoded string.
 */
fun String.urlEncode(): String {
    return URLEncoder.encode(this.replace(" ", "+"), Charsets.UTF_8.name())
}

/**
 * Escape backticks.
 */
val String.escapedBackticks: String
    get() {
        return replace("`", "\\`")
    }

/**
 * Escape the brackets.
 */
val String.escapedBrackets: String
    get() {
        return replace("[", "\\[")
            .replace("]", "\\]")
    }
