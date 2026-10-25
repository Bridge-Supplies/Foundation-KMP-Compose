package bridge.supplies.foundation.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.ExperimentalTime
import java.util.TimeZone as JavaTimeZone

/** Day counts must not depend on the device's time zone (JVM only: the default zone can be changed here). */
class DaysBetweenTimeZoneTests {

    private val zones = listOf(
        "America/New_York", "America/Los_Angeles", "Europe/London", "Europe/Berlin",
        "Australia/Sydney", "Pacific/Auckland", "Asia/Tokyo", "UTC",
    )

    private fun LocalDate.toUtcMs(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    private fun withDefaultZone(id: String, block: () -> Unit) {
        val original = JavaTimeZone.getDefault()
        JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(id))
        try {
            block()
        } finally {
            JavaTimeZone.setDefault(original)
        }
    }

    @Test
    fun countsMatchTheCalendarInEveryZone() {
        val spans = listOf(
            LocalDate(1990, 7, 4) to LocalDate(2026, 1, 15), // summer birthday, winter today
            LocalDate(1990, 1, 15) to LocalDate(2026, 7, 4), // winter birthday, summer today
            LocalDate(2026, 10, 31) to LocalDate(2026, 11, 2), // US fall back
            LocalDate(2026, 4, 4) to LocalDate(2026, 4, 6), // Sydney fall back
        )
        for (zone in zones) {
            withDefaultZone(zone) {
                for ((from, to) in spans) {
                    assertEquals(from.daysUntil(to), daysBetween(from.toUtcMs(), to.toUtcMs()), "$zone: $from to $to")
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun theOldDeviceZoneCountWasOneShort() {
        // Documents the bug this guards against: counting in New York time loses a day for this span
        val from = LocalDate(1990, 7, 4).toUtcMs()
        val to = LocalDate(2026, 1, 15).toUtcMs()
        withDefaultZone("America/New_York") {
            val deviceZoneCount = kotlin.time.Instant.fromEpochMilliseconds(from)
                .daysUntil(kotlin.time.Instant.fromEpochMilliseconds(to), TimeZone.currentSystemDefault())
            assertNotEquals(daysBetween(from, to), deviceZoneCount)
            assertEquals(daysBetween(from, to) - 1, deviceZoneCount)
        }
    }
}
