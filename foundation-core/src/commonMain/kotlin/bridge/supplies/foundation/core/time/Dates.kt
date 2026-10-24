package bridge.supplies.foundation.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// DATE/TIME

fun getDateDisplay(
    date: LocalDate
): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = date.day
    val year = date.year
    return "$month $day, $year"
}

@OptIn(ExperimentalTime::class)
fun getDateTime(timeMs: Long): LocalDate {
    return Instant.fromEpochMilliseconds(timeMs).toLocalDateTime(TimeZone.UTC).date
}

@OptIn(ExperimentalTime::class)
fun getTodayDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

@OptIn(ExperimentalTime::class)
fun getTodayUtcMs(): Long {
    val today = getTodayDate()
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

fun getDaysSinceBirth(birthdayTimeMs: Long): Int {
    val today = getTodayUtcMs()
    val daysSinceBirth = daysBetween(today, birthdayTimeMs) + 1
    return daysSinceBirth
}

@OptIn(ExperimentalTime::class)
fun daysBetween(startMs: Long, endMs: Long): Int {
    val startInstant = Instant.fromEpochMilliseconds(min(startMs, endMs))
    val endInstant = Instant.fromEpochMilliseconds(max(startMs, endMs))
    return startInstant.daysUntil(endInstant, timeZone = TimeZone.currentSystemDefault())
}
