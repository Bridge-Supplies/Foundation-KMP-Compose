package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationOpenURLOptionUniversalLinksOnly

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

actual fun randomUuid(): String = NSUUID().UUIDString()

actual fun browseWeb(
    url: String
): Boolean {
    val nsUrl = NSURL(string = url)
    
    if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        UIApplication.sharedApplication.openURL(nsUrl, mapOf(UIApplicationOpenURLOptionUniversalLinksOnly to false)) { success ->
            if (!success) {
                println("Failed to open url: $nsUrl")
            }
        }
    } else {
        println("Cannot open open url: $nsUrl")
        return false
    }
    
    return true
}

actual fun systemAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    
    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url, mapOf(UIApplicationOpenURLOptionUniversalLinksOnly to false)) { success ->
            if (!success) {
                println("Failed to open app settings")
            }
        }
    } else {
        println("Cannot open app settings URL.")
    }
}