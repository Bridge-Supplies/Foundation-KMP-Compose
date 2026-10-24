package bridge.supplies.foundation

import config.ColorTheme
import config.DarkMode
import config.Palette
import data.SettingKeys
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Persisted settings must never change shape: apps built on Foundation (and their users' saved settings)
 * depend on these exact key names in `settings.preferences_pb`, and enums are stored as ordinals.
 * If a change is ever needed, write a migration.
 */
class StoredKeyStabilityTests {
    
    @Test
    fun settingKeyNamesAreStable() {
        val actual = listOf(
            SettingKeys.desktopResolutionX.name,
            SettingKeys.desktopResolutionY.name,
            SettingKeys.landingTips.name,
            SettingKeys.fullscreenLandscape.name,
            SettingKeys.expressiveAnimation.name,
            SettingKeys.colorTheme.name,
            SettingKeys.paletteStyle.name,
            SettingKeys.darkMode.name,
            SettingKeys.vibration.name,
            SettingKeys.scrollHelpers.name,
            SettingKeys.encryptedShare.name,
        )
        val expected = listOf(
            "Settings_desktop_resolution_x",
            "Settings_desktop_resolution_y",
            "Settings_landing_tips",
            "Settings_fullscreen_landscape",
            "Settings_expressive_animation",
            "Settings_color_theme",
            "Settings_palette_style",
            "Settings_dark_mode",
            "Settings_vibration",
            "Settings_scroll_helpers",
            "Settings_encrypted_share",
        )
        assertEquals(expected, actual)
    }
    
    @Test
    fun enumOrdinalsAreStable() {
        // Stored as ordinals: only ever append new entries
        assertEquals(listOf("AUTO", "RED", "GREEN", "BLUE", "OFF"), ColorTheme.entries.map { it.name })
        assertEquals(listOf("NEUTRAL", "SPOT", "TINTED", "BOLD"), Palette.entries.map { it.name })
        assertEquals(listOf("AUTO", "LIGHT", "DARK", "AMOLED"), DarkMode.entries.map { it.name })
    }
}
