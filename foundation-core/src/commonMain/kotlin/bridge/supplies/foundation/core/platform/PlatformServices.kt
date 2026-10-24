package bridge.supplies.foundation.core.platform

public expect fun randomUuid(): String

/**
 * Opens [url] in the browser, or inside the app when [inApp] is true (Custom Tabs on Android,
 * SFSafariViewController on iOS; desktop always uses the browser). Returns false if nothing could open it.
 */
public expect fun openUrl(url: String, inApp: Boolean = false): Boolean

/** Opens this app's page in the system settings (permissions, notifications, …). */
public expect fun openAppSettings()
