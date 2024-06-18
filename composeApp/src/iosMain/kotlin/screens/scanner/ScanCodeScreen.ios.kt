package screens.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
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
import config.ColorSchemeStyle
import config.getAppliedColorScheme
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.camera_flash_button_text
import foundation.composeapp.generated.resources.import_button_text
import org.jetbrains.compose.resources.stringResource
import qrscanner.QrScanner

@Composable
actual fun CodeScannerLayout(
    modifier: Modifier,
    onVibrate: () -> Unit,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.VARIANT)
    var flashlightOn by remember { mutableStateOf(false) }
    var launchGallery by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
    ) {
        QrScanner(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(1f)
                .clipToBounds()
                .clip(shape = RoundedCornerShape(size = 12.dp)),
            flashlightOn = flashlightOn,
            launchGallery = launchGallery,
            onCompletion = onCompletion,
            onGalleryCallBackHandler = {
                launchGallery = it
            },
            onFailure = onFailure
        )
        
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            IconToggleButton(
                checked = flashlightOn,
                onCheckedChange = { enabled ->
                    flashlightOn = enabled
                    onVibrate()
                }
            ) {
                Icon(
                    imageVector = if (flashlightOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = stringResource(Res.string.camera_flash_button_text)
                )
            }
            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onClick = {
                    launchGallery = true
                    onVibrate()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.buttonColor,
                    contentColor = colorScheme.onButtonColor
                )
            ) {
                Text(stringResource(Res.string.import_button_text))
            }
        }
    }
}