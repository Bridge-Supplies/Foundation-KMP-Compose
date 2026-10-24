package bridge.supplies.foundation.ui.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import org.jetbrains.skia.Image
import java.awt.datatransfer.StringSelection

public actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

@OptIn(ExperimentalComposeUiApi::class)
public actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    clipboard.setClipEntry(ClipEntry(StringSelection(text)))
}
