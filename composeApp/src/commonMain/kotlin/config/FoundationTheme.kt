package config

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.theme_settings_color_theme_auto
import foundation.composeapp.generated.resources.theme_settings_color_theme_blue
import foundation.composeapp.generated.resources.theme_settings_color_theme_green
import foundation.composeapp.generated.resources.theme_settings_color_theme_off
import foundation.composeapp.generated.resources.theme_settings_color_theme_red
import foundation.composeapp.generated.resources.theme_settings_dark_mode_amoled
import foundation.composeapp.generated.resources.theme_settings_dark_mode_auto
import foundation.composeapp.generated.resources.theme_settings_dark_mode_dark
import foundation.composeapp.generated.resources.theme_settings_dark_mode_light
import foundation.composeapp.generated.resources.theme_settings_palette_bold
import foundation.composeapp.generated.resources.theme_settings_palette_neutral
import foundation.composeapp.generated.resources.theme_settings_palette_spot
import foundation.composeapp.generated.resources.theme_settings_palette_tinted
import org.jetbrains.compose.resources.StringResource

@Composable
expect fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
)

enum class ColorTheme(
    val titleRes: StringResource,
    val color: Color
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
    )
}

enum class Palette(
    val titleRes: StringResource,
    val paletteStyle: PaletteStyle
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

enum class DarkMode(
    val titleRes: StringResource
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