package bogus.util

import mu.KLogger

fun KLogger.asFMTLogger(): FMTLoggingAdapter {
    return FMTLoggingAdapter(this)
}

/**
 * Simple contextual builder for fmt logging
 */
class FMTLogBuilder {
    var message: String? = ""
    var context: Map<String, Any?> = mapOf()
}

/**
 * Adapts a KLogger for fmt logging.
 */
class FMTLoggingAdapter(private val log: KLogger) {
    /**
     * Lazy add a log message with throwable payload if isErrorEnabled is true
     */
    fun error(throwable: Throwable, builder: FMTLogBuilder.() -> Unit) {
        log.error(throwable) { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    /**
     * Lazy add a log message with throwable payload if isErrorEnabled is true
     */
    fun error(builder: FMTLogBuilder.() -> Unit) {
        log.error { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    /**
     * Lazy add a log message with throwable payload if isTraceEnabled is true
     */
    fun trace(builder: FMTLogBuilder.() -> Unit) {
        log.trace { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    /**
     * Lazy add a log message with throwable payload if isDebugEnabled is true
     */
    fun debug(builder: FMTLogBuilder.() -> Unit) {
        log.debug { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    /**
     * Lazy add a log message with throwable payload if isWarnEnabled is true
     */
    fun warn(builder: FMTLogBuilder.() -> Unit) {
        log.warn { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    /**
     * Lazy add a log message with throwable payload if isInfoEnabled is true
     */
    fun info(builder: FMTLogBuilder.() -> Unit) {
        log.info { buildMessage(FMTLogBuilder().apply(builder)) }
    }

    private fun buildMessage(builder: FMTLogBuilder): String {
        return builder.message + buildString {
            if (builder.context.isNotEmpty()) {
                append(" — ")

                builder.context.forEach { (key, value) ->
                    val valueStr = value.toString()

                    append(key)
                    append("=")

                    if (valueStr.contains(" ")) {
                        append('"')
                        append(valueStr.replace("\"", "\\\""))
                        append('"')
                    } else {
                        append(valueStr)
                    }

                    append(" ")
                }
            }
        }.dropLast(1)
    }
}