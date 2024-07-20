package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import org.koin.compose.currentKoinScope

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

fun getDateDisplay(
    date: LocalDate
): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val day = date.dayOfMonth
    val year = date.year
    return "$month $day, $year"
}

fun getTodayDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun getTodayUtcMs(): Long {
    val today = getTodayDate()
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}