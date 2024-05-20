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
    flashlightOn: Boolean,
    launchGallery: Boolean,
    onCompletion: (String) -> Unit,
    onGalleryCallBackHandler: (Boolean) -> Unit,
    onFailure: (String) -> Unit
) {
    QrScanner(
        modifier = Modifier
            .clipToBounds()
            .clip(shape = RoundedCornerShape(size = 8.dp)),
        flashlightOn = flashlightOn,
        launchGallery = launchGallery,
        onCompletion = onCompletion,
        onGalleryCallBackHandler = onGalleryCallBackHandler,
        onFailure = onFailure
    )
}