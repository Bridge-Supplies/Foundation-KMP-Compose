package bridge.supplies.foundation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import bridge.supplies.foundation.ui.util.hideAndClearFocus

/**
 * How many sent values [TextInput] remembers while waiting to see them come back from the caller.
 */
private const val MAX_UNCONFIRMED_TEXT_VALUES = 64

@Composable
public fun TextInput(
    modifier: Modifier = Modifier,
    text: String,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxTextLength: Int? = null,
    hintText: String? = null,
    onValueChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Callers often store each change asynchronously, so a value can come back after newer
    // keystrokes.
    // A value this field sent is a late echo and is ignored; any other value is an outside change
    // and applies.
    var noteText by remember {
        mutableStateOf(text)
    }
    val sentValues = remember { ArrayDeque<String>() }

    LaunchedEffect(text) {
        val echo = sentValues.indexOf(text)
        if (echo >= 0) {
            repeat(echo + 1) { sentValues.removeFirst() }
        } else {
            sentValues.clear()
            noteText = text
        }
    }

    val supportingText: @Composable (() -> Unit)? =
        if (maxTextLength != null) {
            { Text("${noteText.length}/$maxTextLength") }
        } else null

    OutlinedTextField(
        modifier = modifier.wrapContentHeight().fillMaxWidth(),
        value = noteText,
        onValueChange = {
            val newText = if (maxTextLength != null) it.take(maxTextLength) else it
            if (newText != noteText) {
                noteText = newText
                if (sentValues.size == MAX_UNCONFIRMED_TEXT_VALUES) sentValues.removeFirst()
                sentValues.addLast(newText)
                onValueChange(newText)
            }
        },
        label = {
            if (hintText != null) {
                Text(hintText)
            }
        },
        keyboardOptions =
            KeyboardOptions(
                autoCorrectEnabled = true,
                capitalization = capitalization,
                keyboardType = keyboardType,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                    keyboardController.hideAndClearFocus(focusManager)
                }
            ),
        minLines = minLines,
        maxLines = maxLines,
        supportingText = supportingText,
    )
}
