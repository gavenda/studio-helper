package bogus.extension.information

import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val EXTENSION_NAME = "information"
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("MMMM d, u HH:mm a O")
    .withZone(ZoneId.systemDefault())