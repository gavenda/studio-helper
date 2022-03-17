package bogus.util

/**
 * Returns this boolean to a "Yes" or "No" string.
 */
fun Boolean.toYesNo(): String {
    if (this) return "Yes"
    return "No"
}
