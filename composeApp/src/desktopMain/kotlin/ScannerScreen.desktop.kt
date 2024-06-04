import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.scanner_unsupported_text
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun QrScannerLayout(
    modifier: Modifier,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(Res.string.scanner_unsupported_text)
        )
    }
}