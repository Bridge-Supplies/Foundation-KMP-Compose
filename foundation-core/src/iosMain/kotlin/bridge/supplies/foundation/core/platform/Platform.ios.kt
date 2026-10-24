package bridge.supplies.foundation.core.platform

import platform.UIKit.UIDevice

public actual fun currentPlatform(): FoundationPlatform = IosPlatform

private object IosPlatform : FoundationPlatform {
    override val type = PlatformType.IOS
    override val name = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val features = setOf(
        PlatformFeature.VIBRATION,
        PlatformFeature.CODE_SCANNING
    )
}
