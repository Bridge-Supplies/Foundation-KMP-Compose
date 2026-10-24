package bridge.supplies.foundation.core.platform

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.UUID

public actual fun randomUuid(): String = UUID.randomUUID().toString()

public actual fun openUrl(url: String, inApp: Boolean): Boolean {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
        return true
    } else return false
}

public actual fun openAppSettings() {
    val appLocation = File(System.getProperty("user.dir"))
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            desktop.browseFileDirectory(appLocation)
        } else if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(appLocation)
        }
    }
}
