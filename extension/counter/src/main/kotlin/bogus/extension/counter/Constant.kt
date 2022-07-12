package bogus.extension.counter

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val EXTENSION_NAME = "counter"
const val PAGINATOR_TIMEOUT = 60L
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.SHORT)
    .withZone(ZoneId.systemDefault())