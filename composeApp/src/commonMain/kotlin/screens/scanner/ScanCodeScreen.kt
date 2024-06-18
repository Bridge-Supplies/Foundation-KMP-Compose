package screens.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import config.ColorSchemeStyle
import config.getAppliedColorScheme
import data.MainViewModel
import data.decryptAndUncompress
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.scanner_error_action
import foundation.composeapp.generated.resources.scanner_error_text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

@Composable
expect fun CodeScannerLayout(
    modifier: Modifier,
    onVibrate: () -> Unit,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
)

@Composable
fun ScanCodeScreen(
    viewModel: MainViewModel,
    snackbarHost: SnackbarHostState,
    onVibrate: () -> Unit,
    onCloseScanner: () -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.VARIANT)
    val coroutineScope = rememberCoroutineScope()
    
    val onScanSuccess: (String) -> Unit = {
        viewModel.setSharedText(it)
        onVibrate()
        onCloseScanner()
    }
    
    val onScanFailure: () -> Unit = {
        coroutineScope.launch(Dispatchers.Main) {
            // clear any existing snackbars
            snackbarHost.currentSnackbarData?.dismiss()
            
            onVibrate()
            val snackbar = snackbarHost.showSnackbar(
                message = getString(Res.string.scanner_error_text),
                actionLabel = getString(Res.string.scanner_error_action),
                duration = SnackbarDuration.Short
            )
            
            if (snackbar == SnackbarResult.ActionPerformed) {
                snackbarHost.currentSnackbarData?.dismiss()
            }
        }
    }
    
    val onScanResult: (String) -> Unit = { scannedString ->
        coroutineScope.launch(Dispatchers.Main) {
            val decryptedCode = scannedString.decryptAndUncompress()
            if (decryptedCode != null) {
                onScanSuccess(decryptedCode.message)
            } else {
                onScanFailure()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .background(colorScheme.backgroundColor)
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CodeScannerLayout(
            modifier = Modifier
                .fillMaxSize(),
            onVibrate = onVibrate,
            onCompletion = {
                onScanResult(it)
            },
            onFailure = {
                onScanFailure()
            }
        )
    }
}