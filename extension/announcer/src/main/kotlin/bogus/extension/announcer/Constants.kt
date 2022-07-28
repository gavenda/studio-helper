package bogus.extension.announcer

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val FRAME_BUFFER_SIZE = 1024
const val EXTENSION_NAME = "announcer"
const val TRANSLATIONS_BUNDLE = "announcer"
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.SHORT)
    .withZone(ZoneId.systemDefault())