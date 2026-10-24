package bridge.supplies.foundation.ui.util

import android.content.ClipData
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    val clipData = ClipData.newPlainText("Copied Text", text)
    clipboard.setClipEntry(ClipEntry(clipData))
}
