package bridge.supplies.foundation.core.config

/**
 * Identity and options of the app built on Foundation. The app creates one and passes it to Foundation at startup.
 */
public data class FoundationConfig(
    /** Display name, e.g. "Adepticadian". */
    public val appName: String,
    /** Application/bundle identifier, e.g. "hd.josh.biorhythms". Also names the desktop data folder. */
    public val packageName: String,
    /** Version name, e.g. "2.2.0". */
    public val version: String,
    /** Build number / versionCode, e.g. "202610230". */
    public val build: String,
    /** Key for encrypted QR sharing. Must be exactly 16 bytes. */
    public val encryptionKey: String,
    /** Store or project link used by "Share app". */
    public val shareUrl: String? = null,
    public val storage: StorageConfig = StorageConfig(),
    public val features: FeatureFlags = FeatureFlags(),
)

/** Where Foundation's shared preferences DataStore lives. */
public data class StorageConfig(
    /** Never change this for an existing app: users' saved settings live in this file. */
    public val fileName: String = DEFAULT_SETTINGS_FILE_NAME,
    /** iOS: directory to store the file in (e.g. an App Group container for widgets). Default: Documents. */
    public val iosDirectory: (() -> String)? = null,
    /** Desktop: directory to store the file in. Default: the per-user app data folder. */
    public val desktopDirectory: (() -> String)? = null,
) {
    public companion object {
        public const val DEFAULT_SETTINGS_FILE_NAME: String = "settings.preferences_pb"
    }
}

/** Optional Foundation features an app can turn off. */
public data class FeatureFlags(
    /** QR sharing: Share tab, encrypted-sharing setting and camera permission prompts. */
    public val qrSharing: Boolean = true,
    /** Tips shown on the landing screen at launch. */
    public val landingTips: Boolean = true,
)
