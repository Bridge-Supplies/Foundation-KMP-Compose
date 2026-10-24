package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import java.io.File

/**
 * Desktop settings live in the per-user app data folder (macOS: ~/Library/Application Support/<packageName>,
 * Windows: %APPDATA%\<packageName>, Linux: $XDG_DATA_HOME or ~/.local/share/<packageName>).
 * Foundation 1.x stored the file relative to the working directory; if found there, it's copied over once.
 */
public actual fun settingsFilePath(config: FoundationConfig): String {
    val directory = File(config.storage.desktopDirectory?.invoke() ?: defaultAppDataDirectory(config.packageName))
    directory.mkdirs()
    val file = File(directory, config.storage.fileName)
    val legacyFile = File(config.storage.fileName)
    if (!file.exists() && legacyFile.isFile) {
        legacyFile.copyTo(file)
    }
    return file.absolutePath
}

private fun defaultAppDataDirectory(packageName: String): String {
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    val base = when {
        os.contains("mac") -> "$home/Library/Application Support"
        os.contains("win") -> System.getenv("APPDATA") ?: home
        else -> System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
    }
    return "$base/$packageName"
}
