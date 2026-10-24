package data

import bridge.supplies.foundation.core.settings.BooleanSetting
import bridge.supplies.foundation.core.settings.EnumSetting
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.core.settings.IntSetting
import bridge.supplies.foundation.core.settings.settingName
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.PlatformType
import config.getPlatform
import kotlinx.coroutines.flow.Flow

/**
 * Persisted settings. Key names and encodings (enums as ordinals) must never change:
 * existing users' settings live under these names (see StoredKeyStabilityTests).
 */
object SettingKeys {
    val desktopResolutionX = IntSetting(settingName("desktop_resolution_x"), 800)
    val desktopResolutionY = IntSetting(settingName("desktop_resolution_y"), 480)
    val landingTips = BooleanSetting(settingName("landing_tips"), true)
    val fullscreenLandscape = BooleanSetting(settingName("fullscreen_landscape"), false)
    val expressiveAnimation = BooleanSetting(settingName("expressive_animation"), true)
    val colorTheme = EnumSetting(
        settingName("color_theme"),
        if (getPlatform().supportsFeature(Feature.DYNAMIC_COLORS)) ColorTheme.AUTO else ColorTheme.OFF,
        ColorTheme.entries
    )
    val paletteStyle = EnumSetting(settingName("palette_style"), Palette.TINTED, Palette.entries)
    val darkMode = EnumSetting(settingName("dark_mode"), DarkMode.AUTO, DarkMode.entries)
    val vibration = BooleanSetting(settingName("vibration"), true)
    val scrollHelpers = BooleanSetting(settingName("scroll_helpers"), getPlatform().type == PlatformType.DESKTOP)
    val encryptedShare = BooleanSetting(settingName("encrypted_share"), true)
}

class DataRepository(
    private val settings: FoundationSettings
) {
    suspend fun setDesktopResolutionX(desktopResolutionX: Int) = settings.set(SettingKeys.desktopResolutionX, desktopResolutionX)
    
    fun getDesktopResolutionXFlow(): Flow<Int> = settings.flow(SettingKeys.desktopResolutionX)
    
    suspend fun setDesktopResolutionY(desktopResolutionY: Int) = settings.set(SettingKeys.desktopResolutionY, desktopResolutionY)
    
    fun getDesktopResolutionYFlow(): Flow<Int> = settings.flow(SettingKeys.desktopResolutionY)
    
    suspend fun setLandingTips(landingTips: Boolean) = settings.set(SettingKeys.landingTips, landingTips)
    
    fun getLandingTipsFlow(): Flow<Boolean> = settings.flow(SettingKeys.landingTips)
    
    suspend fun setFullscreenLandscape(fullscreenLandscape: Boolean) = settings.set(SettingKeys.fullscreenLandscape, fullscreenLandscape)
    
    fun getFullscreenLandscapeFlow(): Flow<Boolean> = settings.flow(SettingKeys.fullscreenLandscape)
    
    suspend fun setExpressiveAnimation(expressiveAnimation: Boolean) = settings.set(SettingKeys.expressiveAnimation, expressiveAnimation)
    
    fun getExpressiveAnimationFlow(): Flow<Boolean> = settings.flow(SettingKeys.expressiveAnimation)
    
    suspend fun setColorTheme(colorTheme: ColorTheme) = settings.set(SettingKeys.colorTheme, colorTheme)
    
    fun getColorThemeFlow(): Flow<ColorTheme> = settings.flow(SettingKeys.colorTheme)
    
    suspend fun setPaletteStyle(paletteStyle: Palette) = settings.set(SettingKeys.paletteStyle, paletteStyle)
    
    fun getPaletteStyleFlow(): Flow<Palette> = settings.flow(SettingKeys.paletteStyle)
    
    suspend fun setDarkMode(darkMode: DarkMode) = settings.set(SettingKeys.darkMode, darkMode)
    
    fun getDarkModeFlow(): Flow<DarkMode> = settings.flow(SettingKeys.darkMode)
    
    suspend fun setVibration(vibration: Boolean) = settings.set(SettingKeys.vibration, vibration)
    
    fun getVibrationFlow(): Flow<Boolean> = settings.flow(SettingKeys.vibration)
    
    suspend fun setScrollHelpers(scrollHelpers: Boolean) = settings.set(SettingKeys.scrollHelpers, scrollHelpers)
    
    fun getScrollHelpersFlow(): Flow<Boolean> = settings.flow(SettingKeys.scrollHelpers)
    
    suspend fun setEncryptedShare(encryptedShare: Boolean) = settings.set(SettingKeys.encryptedShare, encryptedShare)
    
    fun getEncryptedShareFlow(): Flow<Boolean> = settings.flow(SettingKeys.encryptedShare)
}
