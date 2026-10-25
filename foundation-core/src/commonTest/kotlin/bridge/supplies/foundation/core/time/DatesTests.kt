package bridge.supplies.foundation.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlin.test.Test
import kotlin.test.assertEquals

class DatesTests {

    /** Dates are stored as UTC midnight, like birthdays and [getTodayUtcMs]. */
    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    /** Expected values come from calendar arithmetic, which involves no time zone. */
    private fun calendarDays(from: LocalDate, to: LocalDate): Int = from.daysUntil(to)

    @Test
    fun sameDayConsecutiveDaysAndEitherOrder() {
        val a = utcMidnight(2026, 10, 24)
        val b = utcMidnight(2026, 10, 25)
        assertEquals(0, daysBetween(a, a))
        assertEquals(1, daysBetween(a, b))
        assertEquals(1, daysBetween(b, a))
    }

    @Test
    fun daylightSavingChangesDontShiftTheCount() {
        // US clocks fall back on 2026-11-01 and spring forward on 2026-03-08; EU on 2026-10-25 / 2026-03-29;
        // Sydney on 2026-04-05 / 2026-10-04
        val spans = listOf(
            LocalDate(2026, 10, 31) to LocalDate(2026, 11, 2),
            LocalDate(2026, 3, 7) to LocalDate(2026, 3, 9),
            LocalDate(2026, 10, 24) to LocalDate(2026, 10, 26),
            LocalDate(2026, 3, 28) to LocalDate(2026, 3, 30),
            LocalDate(2026, 4, 4) to LocalDate(2026, 4, 6),
            LocalDate(2026, 10, 3) to LocalDate(2026, 10, 5),
        )
        for ((from, to) in spans) {
            assertEquals(2, daysBetween(from.toUtcMs(), to.toUtcMs()), "$from to $to")
        }
    }

    @Test
    fun summerBirthdayViewedInWinter() {
        // The reported case: birthday in daylight-saving time, "today" in standard time
        val birthday = LocalDate(1990, 7, 4)
        val today = LocalDate(2026, 1, 15)
        assertEquals(calendarDays(birthday, today), daysBetween(birthday.toUtcMs(), today.toUtcMs()))
    }

    @Test
    fun leapYearsAndDatesBefore1970() {
        assertEquals(2, daysBetween(utcMidnight(2024, 2, 28), utcMidnight(2024, 3, 1)))
        assertEquals(1, daysBetween(utcMidnight(2023, 2, 28), utcMidnight(2023, 3, 1)))
        assertEquals(2, daysBetween(utcMidnight(2000, 2, 28), utcMidnight(2000, 3, 1)))
        assertEquals(1, daysBetween(utcMidnight(1900, 2, 28), utcMidnight(1900, 3, 1)))
        assertEquals(3653, daysBetween(utcMidnight(1960, 1, 1), utcMidnight(1970, 1, 1)))
        assertEquals(3, daysBetween(utcMidnight(1969, 12, 30), utcMidnight(1970, 1, 2)))
    }

    @Test
    fun longSpansMatchTheCalendar() {
        val pairs = listOf(
            LocalDate(1950, 6, 15) to LocalDate(2026, 12, 1),
            LocalDate(1985, 7, 4) to LocalDate(2026, 2, 1),
            LocalDate(2001, 11, 3) to LocalDate(2026, 6, 21),
        )
        for ((from, to) in pairs) {
            assertEquals(calendarDays(from, to), daysBetween(from.toUtcMs(), to.toUtcMs()), "$from to $to")
        }
    }

    @Test
    fun birthdayTodayIsDayOne() {
        assertEquals(1, getDaysSinceBirth(getTodayUtcMs()))
    }

    private fun LocalDate.toUtcMs(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}
