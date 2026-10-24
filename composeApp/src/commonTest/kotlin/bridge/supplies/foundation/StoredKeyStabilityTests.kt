package bridge.supplies.foundation

import data.StoredValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Persisted preference keys must never change: apps built on Foundation (and their users' saved settings)
 * depend on these exact names in `settings.preferences_pb`. If a rename is ever needed, write a migration.
 */
class StoredKeyStabilityTests {

    @Test
    fun settingKeyNamesAreStable() {
        val actual = listOf(
            StoredValue.DESKTOP_RESOLUTION_X().key.name,
            StoredValue.DESKTOP_RESOLUTION_Y().key.name,
            StoredValue.LANDING_TIPS().key.name,
            StoredValue.FULLSCREEN_LANDSCAPE().key.name,
            StoredValue.EXPRESSIVE_ANIMATION().key.name,
            StoredValue.COLOR_THEME().key.name,
            StoredValue.PALETTE_STYLE().key.name,
            StoredValue.DARK_MODE().key.name,
            StoredValue.VIBRATION().key.name,
            StoredValue.SCROLL_HELPERS().key.name,
            StoredValue.ENCRYPTED_SHARE().key.name,
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
}
