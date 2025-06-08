package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import data.bitmapFromBytes
import qrcode.QRCode

fun generateQrCode(
    text: String,
    colorRgb: Int,
    backgroundColorRgb: Int
): ImageBitmap {
    val qrBytes = QRCode.ofSquares()
        .withColor(colorRgb)
        .withBackgroundColor(backgroundColorRgb)
        .build(text)
        .renderToBytes()
    
    return bitmapFromBytes(qrBytes)
}

@Composable
fun CodeDisplay(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    backgroundColor: Color,
    cardColor: Color
) {
    val qrBytes by rememberSaveable(text, color, backgroundColor) {
        mutableStateOf(
            QRCode.ofSquares()
                .withColor(color.toArgb())
                .withBackgroundColor(backgroundColor.toArgb())
                .build(text)
                .renderToBytes()
        )
    }
    
    val qrBitmap by remember(qrBytes) {
        derivedStateOf {
            bitmapFromBytes(qrBytes)
        }
    }
    
    QrCodeCard(
        modifier = modifier,
        qrBitmap = qrBitmap,
        cardColor = cardColor
    )
}

@Composable
fun QrCodeCard(
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap,
    cardColor: Color
) {
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
            bitmap = qrBitmap,
            contentDescription = "QR code"
        )
    }
}