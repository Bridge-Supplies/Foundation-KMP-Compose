import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import qrscanner.QrScanner

@Composable
actual fun QrScannerLayout(
    modifier: Modifier,
    onCompletion: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val flashlightOn = false
    val launchGallery = false
    
    QrScanner(
        modifier = Modifier
            .clipToBounds()
            .clip(shape = RoundedCornerShape(size = 8.dp)),
        flashlightOn = flashlightOn,
        launchGallery = launchGallery,
        onCompletion = onCompletion,
        onGalleryCallBackHandler = { /*TODO*/ },
        onFailure = onFailure
    )
}