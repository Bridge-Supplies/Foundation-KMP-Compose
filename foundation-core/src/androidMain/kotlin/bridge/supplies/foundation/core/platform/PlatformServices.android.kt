package bridge.supplies.foundation.core.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import java.util.UUID

public actual fun randomUuid(): String = UUID.randomUUID().toString()

public actual fun openUrl(url: String, inApp: Boolean): Boolean {
    val context = FoundationAndroid.appContext
    return try {
        if (inApp) {
            val customTab = CustomTabsIntent.Builder().build()
            customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTab.launchUrl(context, url.toUri())
        } else {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent, null)
        }
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

public actual fun openAppSettings() {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.fromParts("package", FoundationAndroid.appContext.packageName, null)
    }
    FoundationAndroid.appContext.startActivity(intent, null)
}
