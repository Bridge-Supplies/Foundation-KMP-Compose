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
    val nsUrl = NSURL(string = url)
    if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        UIApplication.sharedApplication.openURL(nsUrl, mapOf<Any?, Unit>(), { })
    }
    return true
}

actual fun systemAppSettings() {
    val url = NSURLComponents("app-settings:").URL
    url?.let {
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url, mapOf<Any?, Unit>(), { })
        }
    }
}