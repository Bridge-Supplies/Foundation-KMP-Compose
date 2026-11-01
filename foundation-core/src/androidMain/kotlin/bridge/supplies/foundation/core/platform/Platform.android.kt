package bridge.supplies.foundation.core.platform

import android.os.Build

public actual fun currentPlatform(): FoundationPlatform = AndroidPlatform

private object AndroidPlatform : FoundationPlatform {
    override val type = PlatformType.ANDROID
    override val name = "Android ${Build.VERSION.SDK_INT}"
    private val baseFeatures = buildSet {
        add(PlatformFeature.FULLSCREEN_LANDSCAPE)
        add(PlatformFeature.VIBRATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(PlatformFeature.DYNAMIC_COLORS)
    }
    override val features: Set<PlatformFeature>
        // The QR scanner only opens the back camera, so a front camera or webcam doesn't count
        get() = if (FoundationAndroid.hasBackCamera) baseFeatures + PlatformFeature.CODE_SCANNING else baseFeatures
}
