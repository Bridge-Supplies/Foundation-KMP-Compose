package screens.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.MainViewModel

@Composable
expect fun CodeScannerLayout(
    modifier: Modifier,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
)

@Composable
fun ScanCodeScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit,
    onComplete: (result: String) -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onSecondary
    
    val isPortraitMode = isPortraitMode()
    
    Column(
        modifier = Modifier
            .background(surfaceContainerColor)
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CodeScannerLayout(
            modifier = Modifier
                .fillMaxSize(),
            onCompletion = {
                viewModel.setSharedText(it)
                onComplete(it)
                onVibrate()
            },
            onFailure = {
                viewModel.setSharedText(it)
                onComplete(it)
                onVibrate()
            }
        )
    }
}