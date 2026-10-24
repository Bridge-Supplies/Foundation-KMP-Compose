package data

import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.core.net.toUri
import config.appContext
import java.util.UUID

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}

actual fun randomUuid(): String = UUID.randomUUID().toString()

actual fun browseWeb(
    url: String
): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent, null)
    return true
}

actual fun systemAppSettings() {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.fromParts("package", appContext.packageName, null)
    }
    appContext.startActivity(intent, null)
}

actual suspend fun setClipboardText(clipboard: Clipboard, text: String) {
    val clipData = ClipData.newPlainText("Copied Text", text)
    clipboard.setClipEntry(ClipEntry(clipData))
}