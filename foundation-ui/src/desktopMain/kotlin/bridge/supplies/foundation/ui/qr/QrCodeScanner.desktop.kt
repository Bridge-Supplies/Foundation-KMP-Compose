package bridge.supplies.foundation.ui.qr

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public actual fun QrCodeScanner(
    modifier: Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
) {
    // no camera scanning on desktop (PlatformFeature.CODE_SCANNING is unsupported)
}
