package bridge.supplies.foundation.demo.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.demo.data.MainViewModel
import bridge.supplies.foundation.ui.components.BodyText
import bridge.supplies.foundation.ui.components.TextInput
import bridge.supplies.foundation.ui.shell.LocalNavigator
import bridge.supplies.foundation.ui.shell.ScreenBackHandler
import bridge.supplies.foundation.ui.shell.TopBarAction
import bridge.supplies.foundation.ui.shell.TopBarActions
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.editor_discard
import foundation.composeapp.generated.resources.editor_discard_text
import foundation.composeapp.generated.resources.editor_hint
import foundation.composeapp.generated.resources.editor_keep_editing
import foundation.composeapp.generated.resources.editor_save
import org.jetbrains.compose.resources.stringResource

/**
 * A focused editor: the top bar only, closed with its close button, Save as a top bar action, and a
 * question before unsaved changes are lost.
 */
@Composable
fun HomeEditorScreen(viewModel: MainViewModel, hapticFeedback: () -> Unit) {
    val navigator = LocalNavigator.current
    val saved by viewModel.sharedText.collectAsState()
    var draft by rememberSaveable { mutableStateOf(saved) }
    var askDiscard by rememberSaveable { mutableStateOf(false) }

    TopBarActions(
        listOf(
            TopBarAction(Icons.Filled.Check, Res.string.editor_save) {
                hapticFeedback()
                viewModel.setSharedText(draft)
                navigator.back()
            }
        )
    )
    ScreenBackHandler(enabled = draft != saved) { askDiscard = true }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextInput(
            text = draft,
            minLines = 3,
            maxLines = 6,
            hintText = stringResource(Res.string.editor_hint),
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            text = { BodyText(text = stringResource(Res.string.editor_discard_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        askDiscard = false
                        navigator.back()
                    }
                ) {
                    Text(stringResource(Res.string.editor_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { askDiscard = false }) {
                    Text(stringResource(Res.string.editor_keep_editing))
                }
            },
        )
    }
}
