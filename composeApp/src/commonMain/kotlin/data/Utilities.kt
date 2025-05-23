package data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.compose.currentKoinScope
import kotlin.enums.EnumEntries
import kotlin.math.max
import kotlin.math.min

@Composable
inline fun <reified T : ViewModel> koinViewModel(): T {
    val scope = currentKoinScope()
    return viewModel {
        scope.get<T>()
    }
}

// val keyboardController = LocalSoftwareKeyboardController.current
// val focusManager = LocalFocusManager.current
fun SoftwareKeyboardController?.hideAndClearFocus(focusManager: FocusManager?) {
    this?.hide()
    focusManager?.clearFocus()
}

expect fun bitmapFromBytes(bytes: ByteArray): ImageBitmap

expect fun randomUuid(): String

expect fun browseWeb(url: String): Boolean

expect fun systemAppSettings()


// DATE/TIME

fun getDateDisplay(
    date: LocalDate
): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = date.dayOfMonth
    val year = date.year
    return "$month $day, $year"
}

fun getDateTime(timeMs: Long): LocalDate {
    return Instant.fromEpochMilliseconds(timeMs).toLocalDateTime(TimeZone.UTC).date
}

fun getTodayDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun getTodayUtcMs(): Long {
    val today = getTodayDate()
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

fun getDaysSinceBirth(birthdayTimeMs: Long): Int {
    val today = getTodayUtcMs()
    
    val daysSinceBirth = daysBetween(today, birthdayTimeMs) + 1
    return daysSinceBirth
}

fun daysBetween(startMs: Long, endMs: Long): Int {
    val startInstant = Instant.fromEpochMilliseconds(min(startMs, endMs))
    val endInstant = Instant.fromEpochMilliseconds(max(startMs, endMs))
    return startInstant.daysUntil(endInstant, timeZone = TimeZone.currentSystemDefault())
}


// FORMATTING

fun Int.formatDecimalSeparator(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}


// REMEMBERS

@Composable
fun <T : Enum<T>> rememberSaveableEnumMap(
    enumValues: EnumEntries<T>,
    initialValue: Boolean = false
): SnapshotStateMap<T, Boolean> {
    return rememberSaveable(
        saver = mapSaver(
            save = { stateMap -> stateMap.mapKeys { it.key.name } },
            restore = { savedMap ->
                mutableStateMapOf<T, Boolean>().apply {
                    savedMap.forEach { (keyName, value) ->
                        val key = enumValues.first { it.name == keyName }
                        this[key] = value as Boolean
                    }
                }
            }
        )
    ) {
        mutableStateMapOf<T, Boolean>().apply {
            enumValues.forEach {
                this[it] = initialValue
            }
        }
    }
}