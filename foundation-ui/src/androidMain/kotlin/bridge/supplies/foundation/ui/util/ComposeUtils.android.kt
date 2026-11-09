package bridge.supplies.foundation.ui.util

import android.content.ClipData
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.clipboard_label
import org.jetbrains.compose.resources.getString

public actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}

public actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    val clipData = ClipData.newPlainText(getString(Res.string.clipboard_label), text)
    clipboard.setClipEntry(ClipEntry(clipData))
}
