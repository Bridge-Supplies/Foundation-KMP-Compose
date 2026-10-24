package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun settingsFilePath(config: FoundationConfig): String {
    val directory = config.storage.iosDirectory?.invoke() ?: run {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(documentDirectory).path
    }
    return "$directory/${config.storage.fileName}"
}
