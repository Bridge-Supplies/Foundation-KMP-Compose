package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

actual fun randomUuid(): String = NSUUID().UUIDString()

actual fun browseWeb(
    url: String
): Boolean {
    UIApplication.sharedApplication.openURL(NSURL(string = url))
    return true
}

actual fun systemAppSettings() {
    val url = NSURLComponents("app-settings:").URL
    url?.let {
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}