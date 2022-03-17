package bogus.util

/**
 * Loads any file in resources and returns them as a string.
 */
fun findResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path)!!.readText()
}
