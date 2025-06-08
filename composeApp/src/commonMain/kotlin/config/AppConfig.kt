package config

import androidx.compose.runtime.Composable
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.landing_tip_code_scanning
import foundation.composeapp.generated.resources.landing_tip_dark_mode
import foundation.composeapp.generated.resources.landing_tip_dynamic_colors
import foundation.composeapp.generated.resources.landing_tip_fullscreen_landscape
import foundation.composeapp.generated.resources.landing_tip_theme_colors
import foundation.composeapp.generated.resources.landing_tip_vibration
import org.jetbrains.compose.resources.StringResource
import org.koin.core.module.Module

// System info

enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP
}

val BASE_LANDING_TIPS = listOf(
    // Foundation
    Res.string.landing_tip_dark_mode,
    Res.string.landing_tip_theme_colors,
)

enum class Feature(
    val landingTips: List<StringResource>
) {
    FULLSCREEN_LANDSCAPE(
        listOf(
            Res.string.landing_tip_fullscreen_landscape
        )
    ),
    DYNAMIC_COLORS(
        listOf(
            Res.string.landing_tip_dynamic_colors
        )
    ),
    VIBRATION(
        listOf(
            Res.string.landing_tip_vibration
        )
    ),
    CODE_SCANNING(
        listOf(
            Res.string.landing_tip_code_scanning
        )
    )
}

interface Platform {
    val type: PlatformType
    val name: String
    val version: String // ex: 1.0.0
    val build: String // ex: 202401010
    val shareUrl: String
    val supportedFeatures: List<Feature>
    
    fun supportsFeature(feature: Feature) =
        supportedFeatures.contains(feature)
    
    fun getLandingTips() =
        BASE_LANDING_TIPS + supportedFeatures.flatMap { it.landingTips }
}

expect fun getPlatform(): Platform

@Composable
expect fun isPortraitMode(): Boolean

const val LANDING_SCREEN_REVEAL_DURATION_MS = 1000
const val LANDING_SCREEN_SHORT_DURATION_MS = 0
const val LANDING_SCREEN_LONG_DURATION_MS = 2000


// Koin modules

expect class KoinInitializer {
    fun init()
}

expect val platformModule: Module

expect val dataStoreModule: Module

expect val viewModelModule: Module