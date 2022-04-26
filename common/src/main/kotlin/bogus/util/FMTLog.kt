package bogus.util

import mu.KLogger

fun KLogger.asLogFMT(): LoggerFMT {
    return LoggerFMT(this)
}

class LoggerFMT(private val log: KLogger) {
    fun error(throwable: Throwable, msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.error(throwable) { buildMessage(msg, context) }
    }

    fun error(msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.error { buildMessage(msg, context) }
    }

    fun trace(msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.trace { buildMessage(msg, context) }
    }

    fun debug(msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.debug { buildMessage(msg, context) }
    }

    fun warn(msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.warn { buildMessage(msg, context) }
    }

    fun info(msg: String? = "", context: Map<String, Any?> = mapOf()) {
        log.info { buildMessage(msg, context) }
    }

    private fun buildMessage(msg: String?, context: Map<String, Any?>): String {
        return msg + buildString {
            if (context.isNotEmpty()) {
                append(" — ")

                context.forEach { (key, value) ->
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