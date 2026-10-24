package bridge.supplies.foundation.ui.qr

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.crypto.QrCodec
import bridge.supplies.foundation.ui.components.CircularReveal
import bridge.supplies.foundation.ui.components.QrCodeCard
import bridge.supplies.foundation.ui.components.generateQrCode
import bridge.supplies.foundation.ui.components.showSnackBar
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.scanner_error_action
import bridge.supplies.foundation.ui.resources.scanner_error_text
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.shell.LocalSnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

private const val QR_GENERATION_DEBOUNCE_MS = 300L

/**
 * Camera QR scanner with the camera permission flow (Android/iOS; nothing on desktop).
 * Reports raw scanned text; [ScanQrScreen] decodes it for you.
 */
@Composable
public expect fun QrCodeScanner(
    modifier: Modifier = Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
)

/**
 * Full-screen scanner for QR codes made with [QrCodec] (v2, and legacy v1). Calls [onScanned] once with the first
 * code that decodes as [serializer]; anything else shows a "try again" snackbar.
 */
@Composable
public fun <T> ScanQrScreen(
    serializer: KSerializer<T>,
    onScanned: (T) -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val qrCodec = koinInject<QrCodec>()
    val snackbarHost = LocalSnackbarHostState.current
    val hapticFeedback = rememberHapticFeedback()
    val coroutineScope = rememberCoroutineScope()
    var successfulScan by remember { mutableStateOf(false) }

    val onScanFailure: () -> Unit = {
        coroutineScope.launch {
            showSnackBar(
                message = getString(Res.string.scanner_error_text),
                actionLabel = getString(Res.string.scanner_error_action),
                snackbarHost = snackbarHost,
                hapticFeedback = hapticFeedback
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
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
                QrCodeScanner(
                    onSuccess = { scanned ->
                        if (!successfulScan) {
                            val value = qrCodec.decode(scanned, serializer).getOrNull()
                            if (value != null) {
                                successfulScan = true
                                hapticFeedback()
                                onScanned(value)
                            } else {
                                onScanFailure()
                            }
                        }
                    },
                    onFailure = { onScanFailure() },
                    onDenied = onPermissionDenied
                )
            }
        }
    }
}

/**
 * A QR code for [value], encoded with [QrCodec] (encrypted when the user's "Encrypted sharing" setting is on, unless
 * [encrypt] says otherwise) in the theme's colors. Regenerates after input pauses, keeping the previous code visible
 * meanwhile; shows a spinner before the first code and nothing when [value] is null.
 */
@Composable
public fun <T> ShareQrCode(
    value: T?,
    serializer: KSerializer<T>,
    modifier: Modifier = Modifier,
    encrypt: Boolean? = null,
) {
    val qrCodec = koinInject<QrCodec>()
    val encryptedShare by foundationViewModel().setting(FoundationSettingKeys.encryptedShare).collectAsState()
    val useEncryption = encrypt ?: encryptedShare
    val color = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.onPrimary
    var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Each change restarts this effect, cancelling any in-flight generation,
    // so only the latest value is encoded and rendered once input pauses.
    LaunchedEffect(value, useEncryption, color, backgroundColor) {
        if (value == null) {
            qrBitmap = null
            return@LaunchedEffect
        }
        delay(QR_GENERATION_DEBOUNCE_MS)
        qrBitmap = withContext(Dispatchers.Default) {
            generateQrCode(
                text = qrCodec.encode(value, serializer, useEncryption),
                colorRgb = color.toArgb(),
                backgroundColorRgb = backgroundColor.toArgb()
            )
        }
    }

    Crossfade(
        modifier = modifier,
        targetState = qrBitmap
    ) { qr ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                qr != null -> QrCodeCard(
                    qrBitmap = qr,
                    cardColor = MaterialTheme.colorScheme.onSurface
                )

                value != null -> Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                else -> Spacer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
