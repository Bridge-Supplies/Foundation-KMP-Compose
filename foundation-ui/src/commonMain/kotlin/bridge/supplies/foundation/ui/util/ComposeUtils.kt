package bridge.supplies.foundation.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.currentKoinScope
import kotlin.enums.EnumEntries

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

expect suspend fun setClipboardText(clipboard: Clipboard, text: String)

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
