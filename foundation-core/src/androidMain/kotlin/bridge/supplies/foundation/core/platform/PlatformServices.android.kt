package bridge.supplies.foundation.core.platform

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import java.util.UUID

actual fun randomUuid(): String = UUID.randomUUID().toString()

actual fun browseWeb(
    url: String
): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    FoundationAndroid.appContext.startActivity(intent, null)
    return true
}

actual fun systemAppSettings() {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.fromParts("package", FoundationAndroid.appContext.packageName, null)
    }
    FoundationAndroid.appContext.startActivity(intent, null)
}
