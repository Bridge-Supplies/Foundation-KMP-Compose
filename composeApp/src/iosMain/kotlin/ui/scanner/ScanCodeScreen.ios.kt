package ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.camera_flash_button_text
import foundation.composeapp.generated.resources.import_button_text
import org.jetbrains.compose.resources.stringResource
import qrscanner.QrScanner
import ui.CircularReveal
import ui.TextButton
import ui.ToggleableIcon

@Composable
actual fun CodeScannerLayout(
    modifier: Modifier,
    onVibrate: () -> Unit,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    var flashlightOn by remember { mutableStateOf(false) }
    var launchGallery by remember { mutableStateOf(false) }
    
    val shape = RoundedCornerShape(size = 12.dp)
    
    Column(
        modifier = modifier
    ) {
        CircularReveal(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .clip(shape = shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            startDelayMs = 500,
            revealDurationMs = 800
        ) {
            QrScanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .clipToBounds()
                    .clip(shape = shape),
                flashlightOn = flashlightOn,
                launchGallery = launchGallery,
                onCompletion = onCompletion,
                onGalleryCallBackHandler = {
                    launchGallery = it
                },
                onFailure = onFailure
            )
        }
        
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            ToggleableIcon(
                enabled = flashlightOn,
                enabledVector = Icons.Filled.FlashOn,
                disabledVector = Icons.Filled.FlashOff,
                contentDescription = stringResource(Res.string.camera_flash_button_text)
            ) { enabled ->
                flashlightOn = enabled
                onVibrate()
            }
            
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = stringResource(Res.string.import_button_text)
            ) {
                launchGallery = true
                onVibrate()
            }
        }
    }
}