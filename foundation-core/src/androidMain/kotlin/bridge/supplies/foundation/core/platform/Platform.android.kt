package bridge.supplies.foundation.core.platform

import android.os.Build

public actual fun currentPlatform(): FoundationPlatform = AndroidPlatform

private object AndroidPlatform : FoundationPlatform {
    override val type = PlatformType.ANDROID
    override val name = "Android ${Build.VERSION.SDK_INT}"
    override val features = buildSet {
        add(PlatformFeature.FULLSCREEN_LANDSCAPE)
        add(PlatformFeature.VIBRATION)
        add(PlatformFeature.CODE_SCANNING)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(PlatformFeature.DYNAMIC_COLORS)
    }
}
