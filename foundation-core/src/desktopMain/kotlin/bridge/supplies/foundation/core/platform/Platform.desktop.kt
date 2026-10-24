package bridge.supplies.foundation.core.platform

public actual fun currentPlatform(): FoundationPlatform = DesktopPlatform

private object DesktopPlatform : FoundationPlatform {
    override val type = PlatformType.DESKTOP
    override val name = "Java ${System.getProperty("java.version")}"
    override val features = emptySet<PlatformFeature>()
}
