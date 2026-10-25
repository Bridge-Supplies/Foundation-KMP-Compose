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

public fun getDateDisplay(
    date: LocalDate
): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = date.day
    val year = date.year
    return "$month $day, $year"
}

@OptIn(ExperimentalTime::class)
public fun getDateTime(timeMs: Long): LocalDate {
    return Instant.fromEpochMilliseconds(timeMs).toLocalDateTime(TimeZone.UTC).date
}

@OptIn(ExperimentalTime::class)
public fun getTodayDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

@OptIn(ExperimentalTime::class)
public fun getTodayUtcMs(): Long {
    val today = getTodayDate()
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/** Day number since a birthday stored as UTC midnight: the birthday itself is day 1. */
public fun getDaysSinceBirth(birthdayTimeMs: Long): Int {
    val today = getTodayUtcMs()
    val daysSinceBirth = daysBetween(today, birthdayTimeMs) + 1
    return daysSinceBirth
}

/**
 * Whole days between two timestamps, in either order. Dates are stored as UTC midnight
 * ([getTodayUtcMs], date pickers), so days are counted in UTC: counting in the device's
 * time zone came out one day short across daylight-saving changes.
 */
@OptIn(ExperimentalTime::class)
public fun daysBetween(startMs: Long, endMs: Long): Int {
    val startInstant = Instant.fromEpochMilliseconds(min(startMs, endMs))
    val endInstant = Instant.fromEpochMilliseconds(max(startMs, endMs))
    return startInstant.daysUntil(endInstant, timeZone = TimeZone.UTC)
}
