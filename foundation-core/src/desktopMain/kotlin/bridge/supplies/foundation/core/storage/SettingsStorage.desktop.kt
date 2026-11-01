package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Desktop settings live in the per-user app data folder (macOS: ~/Library/Application Support/<packageName>,
 * Windows: %APPDATA%\<packageName>, Linux: $XDG_DATA_HOME or ~/.local/share/<packageName>).
 * Foundation 1.x stored the file relative to the working directory; if found there, it's copied over once.
 */
internal actual fun settingsFilePath(config: FoundationConfig): String {
    val directory = File(config.storage.desktopDirectory?.invoke() ?: defaultAppDataDirectory(config.packageName))
    directory.mkdirs()
    val file = File(directory, config.storage.fileName)
    val legacyFile = File(config.storage.fileName)
    if (!file.exists() && legacyFile.isFile) {
        // Copied under a temporary name first: a partial copy must never look like the settings file
        val partial = File(directory, "${config.storage.fileName}.tmp")
        legacyFile.copyTo(partial, overwrite = true)
        Files.move(partial.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
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
