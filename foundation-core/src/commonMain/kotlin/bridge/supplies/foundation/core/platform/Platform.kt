package bridge.supplies.foundation.core.platform

public enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP
}

/** Optional device capabilities. Settings, landing tips and screens adapt to what the platform supports. */
public enum class PlatformFeature {
    FULLSCREEN_LANDSCAPE,
    DYNAMIC_COLORS,
    VIBRATION,
    CODE_SCANNING
}

/** The device the app runs on. */
public interface FoundationPlatform {
    public val type: PlatformType
    
    /** OS name and version, e.g. "Android 36" or "iOS 27.0". */
    public val name: String
    public val features: Set<PlatformFeature>
    
    public fun supports(feature: PlatformFeature): Boolean = feature in features
}

public expect fun currentPlatform(): FoundationPlatform
