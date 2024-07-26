package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.UUID

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

actual fun randomUuid(): String = UUID.randomUUID().toString()

actual fun browseWeb(
    url: String
): Boolean {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
        return true
    } else return false
}

actual fun systemAppSettings() {
    val appLocation = File(System.getProperty("user.dir"))
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            desktop.browseFileDirectory(appLocation)
        } else if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(appLocation)
        }
    }
}