package bridge.supplies.foundation.core.platform

import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationOpenURLOptionUniversalLinksOnly
import platform.UIKit.UIViewController
import platform.SafariServices.SFSafariViewController

public actual fun randomUuid(): String = NSUUID().UUIDString()

public actual fun openUrl(url: String, inApp: Boolean): Boolean {
    val nsUrl = NSURL(string = url)
    
    if (inApp && (url.startsWith("https://") || url.startsWith("http://"))) {
        val presenter = topViewController()
        if (presenter != null) {
            presenter.presentViewController(SFSafariViewController(uRL = nsUrl), animated = true, completion = null)
            return true
        }
    }
    
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

public actual fun openAppSettings() {
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

@Suppress("DEPRECATION") // keyWindow: fine for single-scene apps
private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
