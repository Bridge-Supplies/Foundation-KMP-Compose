package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSUserDomainMask

/**
 * iOS settings live in Application Support (backed up, not visible in the Files app). Builds before
 * Foundation 2.2.0 used Documents; a file found there is moved over once.
 */
internal actual fun settingsFilePath(config: FoundationConfig): String {
    config.storage.iosDirectory?.invoke()?.let {
        return "$it/${config.storage.fileName}"
    }
    return settingsFileIn(
        directory = requireNotNull(userDirectory(NSApplicationSupportDirectory)),
        legacyDirectory = userDirectory(NSDocumentDirectory),
        fileName = config.storage.fileName,
    )
}

/**
 * Path of [fileName] in [directory], which is created if missing. If the file isn't there yet but
 * is in [legacyDirectory], it's moved over first. If that move fails, the legacy file is used where
 * it is, so the settings are never lost.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun settingsFileIn(directory: String, legacyDirectory: String?, fileName: String): String {
    val fileManager = NSFileManager.defaultManager
    val file = "$directory/$fileName"
    fileManager.createDirectoryAtPath(
        directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    val legacyFile = legacyDirectory?.let { "$it/$fileName" }
    if (
        legacyFile != null &&
            legacyFile != file &&
            !fileManager.fileExistsAtPath(file) &&
            fileManager.fileExistsAtPath(legacyFile)
    ) {
        if (!fileManager.moveItemAtPath(legacyFile, toPath = file, error = null)) {
            return legacyFile
        }
    }
    return file
}

@OptIn(ExperimentalForeignApi::class)
private fun userDirectory(directory: NSSearchPathDirectory): String? =
    NSFileManager.defaultManager
        .URLForDirectory(
            directory = directory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        ?.path
