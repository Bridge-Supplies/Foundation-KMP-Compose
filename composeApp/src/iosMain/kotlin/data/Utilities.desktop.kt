package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}