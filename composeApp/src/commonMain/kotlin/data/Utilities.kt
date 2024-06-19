package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

fun getDateDisplayString(
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault()
): String {
    return now.toLocalDateTime(zone).toString().substringBefore('T')
}

expect fun bitmapFromBytes(bytes: ByteArray): ImageBitmap