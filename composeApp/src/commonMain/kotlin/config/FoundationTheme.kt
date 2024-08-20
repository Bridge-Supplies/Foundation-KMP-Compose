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
import foundation.composeapp.generated.resources.theme_settings_dark_mode_auto
import foundation.composeapp.generated.resources.theme_settings_dark_mode_dark
import foundation.composeapp.generated.resources.theme_settings_dark_mode_light
import foundation.composeapp.generated.resources.theme_settings_palette_neutral
import foundation.composeapp.generated.resources.theme_settings_palette_tonal
import foundation.composeapp.generated.resources.theme_settings_palette_vibrant
import org.jetbrains.compose.resources.StringResource

@Composable
expect fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
)

enum class ColorTheme(
    val stringRes: StringResource,
    val color: Color
) {
    AUTO(
        Res.string.theme_settings_color_theme_auto,
        Color.White
    ),
    RED(
        Res.string.theme_settings_color_theme_red,
        Color.Red
    ),
    GREEN(
        Res.string.theme_settings_color_theme_green,
        Color.Green
    ),
    BLUE(
        Res.string.theme_settings_color_theme_blue,
        Color.Blue
    ),
    OFF(
        Res.string.theme_settings_color_theme_off,
        Color.Black
    )
}

enum class Palette(
    val stringRes: StringResource,
    val paletteStyle: PaletteStyle
) {
    NEUTRAL(
        Res.string.theme_settings_palette_neutral,
        PaletteStyle.Neutral
    ),
    TONAL(
        Res.string.theme_settings_palette_tonal,
        PaletteStyle.TonalSpot
    ),
    VIBRANT(
        Res.string.theme_settings_palette_vibrant,
        PaletteStyle.Vibrant
    )
}

enum class DarkMode(
    val stringRes: StringResource
) {
    AUTO(
        Res.string.theme_settings_dark_mode_auto
    ),
    LIGHT(
        Res.string.theme_settings_dark_mode_light
    ),
    DARK(
        Res.string.theme_settings_dark_mode_dark
    )
}