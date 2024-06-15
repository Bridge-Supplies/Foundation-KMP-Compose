package data

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
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

fun todaysDate(): String {
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()
    return now.toLocalDateTime(zone).toString().substringBefore('T')
}