import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.generate_button_text
import foundation.composeapp.generated.resources.generate_qr_code_text
import foundation.composeapp.generated.resources.scanner_button_text
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import qrcode.QRCode

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScannerScreen(
    viewModel: MainViewModel
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onSecondary
    
    var isScanning by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceContainerColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (getPlatform().supportsQrGeneration) {
            var inputText by remember { mutableStateOf("") }
            
            val qrBytes by remember(inputText) {
                derivedStateOf {
                    val bytes = QRCode.ofSquares()
                        .withBackgroundColor(onPrimaryColor.toArgb())
                        .withColor(primaryColor.toArgb())
                        .build(inputText)
                        .renderToBytes()
                    bitmapFromBytes(bytes)
                }
            }
            
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                value = inputText,
                singleLine = true,
                maxLines = 1,
                label = { Text(stringResource(Res.string.generate_qr_code_text)) },
                onValueChange = {
                    inputText = it
                }
            )
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = onSurfaceColor
                )
            ) {
                if (!isScanning) {
                    Image(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        bitmap = qrBytes,
                        contentDescription = inputText
                    )
                } else {
                    QrScannerLayout(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        flashlightOn = false,
                        launchGallery = false,
                        onCompletion = { result ->
                            inputText = result
                            isScanning = false
                            
                            if (viewModel.useVibration.value) {
                                hapticFeedback(haptics)
                            }
                        },
                        onGalleryCallBackHandler = { },
                        onFailure = {
                            inputText = it
                            isScanning = false
                            
                            if (viewModel.useVibration.value) {
                                hapticFeedback(haptics)
                            }
                        }
                    )
                }
            }
        }
        
        if (getPlatform().supportsQrScanning) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = {
                    isScanning = !isScanning
                    
                    if (viewModel.useVibration.value) {
                        hapticFeedback(haptics)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                )
            ) {
                Text(
                    stringResource(
                        if (isScanning) Res.string.generate_button_text else Res.string.scanner_button_text
                    )
                )
            }
        }
    }
}

@Composable
expect fun QrScannerLayout(
    modifier: Modifier,
    flashlightOn: Boolean,
    launchGallery: Boolean,
    onCompletion: (String) -> Unit,
    onGalleryCallBackHandler: (Boolean) -> Unit,
    onFailure: (String) -> Unit
)