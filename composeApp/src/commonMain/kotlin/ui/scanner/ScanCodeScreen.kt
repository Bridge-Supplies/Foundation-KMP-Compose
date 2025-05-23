package ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import data.MainViewModel
import data.decryptAndUncompress
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.scanner_error_action
import foundation.composeapp.generated.resources.scanner_error_text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import ui.CircularReveal
import ui.showSnackBar

@Composable
expect fun CodeScanner(
    modifier: Modifier = Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
)

@Composable
fun ScanCodeScreen(
    viewModel: MainViewModel,
    snackbarHost: SnackbarHostState,
    hapticFeedback: () -> Unit,
    onSuccessfulScan: (String) -> Unit,
    onPermissionDenied: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var successfulScan by remember { mutableStateOf(false) }
    
    val onScanFailure: () -> Unit = {
        coroutineScope.launch(Dispatchers.Main) {
            showSnackBar(
                message = getString(Res.string.scanner_error_text),
                actionLabel = getString(Res.string.scanner_error_action),
                snackbarHost = snackbarHost,
                hapticFeedback = hapticFeedback
            )
        }
    }
    
    val onScanSuccess: (String) -> Unit = { scannedString ->
        if (!successfulScan) {
            coroutineScope.launch(Dispatchers.Main) {
                val decryptedCode = scannedString.decryptAndUncompress()
                if (decryptedCode?.message != null) {
                    val message = decryptedCode.message
                    successfulScan = true
                    
                    viewModel.setSharedText(message)
                    
                    hapticFeedback()
                    onSuccessfulScan(message)
                } else {
                    onScanFailure()
                }
            }
        }
    }
    
    CodeScannerCamera(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onSuccess = {
            onScanSuccess(it)
        },
        onFailure = {
            onScanFailure()
        },
        onDenied = {
            onPermissionDenied()
        }
    )
}

@Composable
fun CodeScannerCamera(
    modifier: Modifier = Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        CircularReveal(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            startDelayMs = 500,
            revealDurationMs = 800
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CodeScanner(
                    onSuccess = onSuccess,
                    onFailure = onFailure,
                    onDenied = onDenied
                )
            }
        }
    }
}