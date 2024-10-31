package ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MainViewModel
import data.decryptAndUncompress
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.scanner_done
import foundation.composeapp.generated.resources.scanner_error_action
import foundation.composeapp.generated.resources.scanner_error_text
import foundation.composeapp.generated.resources.scanner_success_text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import ui.showSnackBar

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
    val coroutineScope = rememberCoroutineScope()
    
    val onScanSuccess: (String) -> Unit = {
        viewModel.setSharedText(it)
        coroutineScope.launch(Dispatchers.Main) {
            showSnackBar(
                message = getString(Res.string.scanner_success_text),
                actionLabel = getString(Res.string.scanner_done),
                snackbarHost = snackbarHost,
                onVibrate = onVibrate,
                onComplete = onCloseScanner
            )
        }
    }
    
    val onScanFailure: () -> Unit = {
        coroutineScope.launch(Dispatchers.Main) {
            showSnackBar(
                message = getString(Res.string.scanner_error_text),
                actionLabel = getString(Res.string.scanner_error_action),
                snackbarHost = snackbarHost,
                onVibrate = onVibrate
            )
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
            .background(MaterialTheme.colorScheme.background)
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