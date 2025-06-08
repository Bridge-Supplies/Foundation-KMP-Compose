package ui.scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CodeScanner(
    modifier: Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
) {
    // no-op
}