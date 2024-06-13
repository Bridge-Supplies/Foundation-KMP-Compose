package screens.scanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import config.Feature
import config.bitmapFromBytes
import config.isPortraitMode
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.generate_qr_code_text
import foundation.composeapp.generated.resources.scanner_button_text
import org.jetbrains.compose.resources.stringResource
import qrcode.QRCode

@Composable
fun ShareCodeScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onSecondary
    
    val isPortraitMode = isPortraitMode()
    
    // TODO: use debounce to delay code generation while typing
    val inputText by viewModel.sharedText.collectAsState()
    
    if (isPortraitMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceContainerColor)
                .padding(
                    vertical = 16.dp,
                    horizontal = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CodeDisplay(
                modifier = Modifier
                    .weight(2f)
                    .wrapContentHeight()
                    .padding(bottom = 8.dp),
                text = inputText,
                isPortraitMode = isPortraitMode,
                color = primaryColor,
                backgroundColor = onPrimaryColor,
                cardColor = onSurfaceColor
            )
            
            CodeReader(
                modifier = Modifier
                    .weight(1f),
                text = inputText,
                supportsScanning = viewModel.supportsFeature(Feature.CODE_SCANNING),
                setSharedText = {
                    viewModel.setSharedText(it)
                },
                onClickScanner = {
                    onVibrate()
                    onNavigateToScanner()
                }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceContainerColor)
                .padding(
                    vertical = 16.dp,
                    horizontal = viewModel.platform.landscapeContentPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CodeDisplay(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .wrapContentWidth(),
                text = inputText,
                isPortraitMode = isPortraitMode,
                color = primaryColor,
                backgroundColor = onPrimaryColor,
                cardColor = onSurfaceColor
            )
            
            CodeReader(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                text = inputText,
                supportsScanning = viewModel.supportsFeature(Feature.CODE_SCANNING),
                setSharedText = {
                    viewModel.setSharedText(it)
                },
                onClickScanner = {
                    onVibrate()
                    onNavigateToScanner()
                }
            )
        }
    }
}

@Composable
fun CodeDisplay(
    modifier: Modifier,
    text: String,
    isPortraitMode: Boolean,
    color: Color,
    backgroundColor: Color,
    cardColor: Color
) {
    val qrBytes by remember(text) {
        derivedStateOf {
            val bytes = QRCode.ofSquares()
                .withColor(color.toArgb())
                .withBackgroundColor(backgroundColor.toArgb())
                .build(text)
                .renderToBytes()
            bitmapFromBytes(bytes)
        }
    }
    
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Image(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .padding(8.dp),
            bitmap = qrBytes,
            contentDescription = text
        )
    }
}

@Composable
fun CodeReader(
    modifier: Modifier,
    text: String,
    supportsScanning: Boolean,
    setSharedText: (String) -> Unit,
    onClickScanner: () -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onSecondary
    
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = if (supportsScanning) 8.dp else 0.dp),
            value = text,
            label = { Text(stringResource(Res.string.generate_qr_code_text)) },
            onValueChange = {
                setSharedText(it)
            }
        )
        
        if (supportsScanning) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    onClickScanner()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                )
            ) {
                Text(stringResource(Res.string.scanner_button_text))
            }
        }
    }
}