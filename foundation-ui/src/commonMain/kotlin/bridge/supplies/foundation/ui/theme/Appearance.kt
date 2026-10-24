package bridge.supplies.foundation.ui.theme

import androidx.compose.ui.graphics.Color
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_auto
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_blue
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_green
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_off
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_red
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_amoled
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_auto
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_dark
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_light
import bridge.supplies.foundation.ui.resources.theme_settings_palette_bold
import bridge.supplies.foundation.ui.resources.theme_settings_palette_neutral
import bridge.supplies.foundation.ui.resources.theme_settings_palette_spot
import bridge.supplies.foundation.ui.resources.theme_settings_palette_tinted
import com.materialkolor.PaletteStyle
import org.jetbrains.compose.resources.StringResource

// Stored as ordinals (see FoundationSettingKeys): only ever append entries

/** Color source: the system's dynamic colors (Android 12+), a preset seed color, or monochrome. */
public enum class ColorTheme(
    public val titleRes: StringResource,
    /** Default seed color. Apps can override presets with [ThemeConfig.presetColors]. */
    public val color: Color
) {
    AUTO(
        Res.string.theme_settings_color_theme_auto,
        Color.White
    ),
    RED(
        Res.string.theme_settings_color_theme_red,
        Color(255, 23, 68)
    ),
    GREEN(
        Res.string.theme_settings_color_theme_green,
        Color(56, 142, 60)
    ),
    BLUE(
        Res.string.theme_settings_color_theme_blue,
        Color(2, 136, 209)
    ),
    OFF(
        Res.string.theme_settings_color_theme_off,
        Color.Black
    );

    public val isPreset: Boolean get() = this == RED || this == GREEN || this == BLUE
}

/** How strongly a preset color tints the scheme. */
public enum class Palette(
    public val titleRes: StringResource,
    internal val paletteStyle: PaletteStyle
) {
    NEUTRAL(
        Res.string.theme_settings_palette_neutral,
        PaletteStyle.Neutral
    ),
    SPOT(
        Res.string.theme_settings_palette_spot,
        PaletteStyle.Rainbow
    ),
    TINTED(
        Res.string.theme_settings_palette_tinted,
        PaletteStyle.TonalSpot
    ),
    BOLD(
        Res.string.theme_settings_palette_bold,
        PaletteStyle.Vibrant
    )
}

public enum class DarkMode(
    public val titleRes: StringResource
) {
    AUTO(
        Res.string.theme_settings_dark_mode_auto
    ),
    LIGHT(
        Res.string.theme_settings_dark_mode_light
    ),
    DARK(
        Res.string.theme_settings_dark_mode_dark
    ),
    AMOLED(
        Res.string.theme_settings_dark_mode_amoled
    )
}
