package bridge.supplies.foundation.ui.settings

import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.core.platform.PlatformType
import bridge.supplies.foundation.core.platform.currentPlatform
import bridge.supplies.foundation.core.settings.BooleanSetting
import bridge.supplies.foundation.core.settings.EnumSetting
import bridge.supplies.foundation.core.settings.IntSetting
import bridge.supplies.foundation.core.settings.settingName
import bridge.supplies.foundation.ui.theme.ColorTheme
import bridge.supplies.foundation.ui.theme.DarkMode
import bridge.supplies.foundation.ui.theme.Palette

/**
 * Foundation's built-in settings. Key names and encodings (enums as ordinals) must never change:
 * existing users' settings live under these names (see StoredKeyStabilityTests).
 */
public object FoundationSettingKeys {
    public val desktopResolutionX: IntSetting = IntSetting(settingName("desktop_resolution_x"), 800)
    public val desktopResolutionY: IntSetting = IntSetting(settingName("desktop_resolution_y"), 480)
    public val landingTips: BooleanSetting = BooleanSetting(settingName("landing_tips"), true)
    public val fullscreenLandscape: BooleanSetting = BooleanSetting(settingName("fullscreen_landscape"), false)
    public val expressiveAnimation: BooleanSetting = BooleanSetting(settingName("expressive_animation"), true)
    public val colorTheme: EnumSetting<ColorTheme> = EnumSetting(
        settingName("color_theme"),
        if (currentPlatform().supports(PlatformFeature.DYNAMIC_COLORS)) ColorTheme.AUTO else ColorTheme.OFF,
        ColorTheme.entries
    )
    public val paletteStyle: EnumSetting<Palette> = EnumSetting(settingName("palette_style"), Palette.TINTED, Palette.entries)
    public val darkMode: EnumSetting<DarkMode> = EnumSetting(settingName("dark_mode"), DarkMode.AUTO, DarkMode.entries)
    public val vibration: BooleanSetting = BooleanSetting(settingName("vibration"), true)
    public val scrollHelpers: BooleanSetting = BooleanSetting(settingName("scroll_helpers"), currentPlatform().type == PlatformType.DESKTOP)
    public val encryptedShare: BooleanSetting = BooleanSetting(settingName("encrypted_share"), true)
}
