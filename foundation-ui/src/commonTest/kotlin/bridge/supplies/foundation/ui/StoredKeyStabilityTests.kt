package bridge.supplies.foundation.ui

import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.theme.ColorTheme
import bridge.supplies.foundation.ui.theme.DarkMode
import bridge.supplies.foundation.ui.theme.Palette
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
            FoundationSettingKeys.desktopResolutionX.name,
            FoundationSettingKeys.desktopResolutionY.name,
            FoundationSettingKeys.landingTips.name,
            FoundationSettingKeys.fullscreenLandscape.name,
            FoundationSettingKeys.expressiveAnimation.name,
            FoundationSettingKeys.colorTheme.name,
            FoundationSettingKeys.paletteStyle.name,
            FoundationSettingKeys.darkMode.name,
            FoundationSettingKeys.vibration.name,
            FoundationSettingKeys.scrollHelpers.name,
            FoundationSettingKeys.encryptedShare.name,
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
