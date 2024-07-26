package config

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import org.koin.core.module.Module

// System info

enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP
}

enum class Feature {
    FULLSCREEN_LANDSCAPE,
    DYNAMIC_COLORS,
    VIBRATION,
    CODE_SCANNING,
    CODE_UPLOADING
}

interface Platform {
    val type: PlatformType
    val name: String
    val version: String // ex: 1.0.0
    val build: String // ex: 202401010
    val shareUrl: String
    val landscapeContentPadding: Dp
    val supportedFeatures: List<Feature>
    
    fun supportsFeature(feature: Feature) =
        supportedFeatures.contains(feature)
}

expect fun getPlatform(): Platform

data class ScreenSizeInfo(
    val pxHeight: Int,
    val pxWidth: Int,
    val dpHeight: Dp,
    val dpWidth: Dp
)

@Composable
expect fun getScreenSizeInfo(): ScreenSizeInfo

@Composable
expect fun isPortraitMode(): Boolean


// Koin modules

expect class KoinInitializer {
    fun init()
}

expect val platformModule: Module

expect val dataStoreModule: Module

expect val viewModelModule: Module