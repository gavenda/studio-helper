package bogus.util

inline fun failSilently(body: () -> Unit) {
    try {
        body()
    } catch (_: Exception) {}
}