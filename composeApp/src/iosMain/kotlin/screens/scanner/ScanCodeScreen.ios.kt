package screens.scanner

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import qrscanner.QrScanner

@Composable
actual fun CodeScannerLayout(
    modifier: Modifier,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val flashlightOn by remember { mutableStateOf(false) }
    val launchGallery by remember { mutableStateOf(false) }
    
    QrScanner(
        modifier = Modifier
            .clipToBounds()
            .clip(shape = RoundedCornerShape(size = 16.dp)),
        flashlightOn = flashlightOn,
        launchGallery = launchGallery,
        onCompletion = onCompletion,
        onGalleryCallBackHandler = { /*TODO*/ },
        onFailure = onFailure
    )
}