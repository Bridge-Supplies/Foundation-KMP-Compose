package bridge.supplies.foundation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography as MaterialTypography
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import com.materialkolor.PaletteStyle
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.rememberDynamicColorScheme

/** App-level theme options. Game- or brand-specific colors (units, terrain, …) are app constants, not theme. */
public data class ThemeConfig(
    /** Seed colors replacing Foundation's Fire/Earth/Water presets, e.g. `mapOf(ColorTheme.RED to myRed)`. */
    val presetColors: Map<ColorTheme, Color> = emptyMap(),
    val typography: MaterialTypography = Typography,
    /** Material 3 Expressive motion: always on (the default), always off, or the user's choice in Settings. */
    val expressiveAnimation: ExpressiveAnimation = ExpressiveAnimation.ON,
)

/** How the app picks between Material 3's expressive and standard motion schemes. */
public enum class ExpressiveAnimation {
    /** Always expressive motion; Settings shows no toggle. */
    ON,

    /** Always standard motion; Settings shows no toggle. */
    OFF,

    /** Settings shows a toggle (on until the user turns it off). */
    USER_CHOICE,
}

/** The theme's expressive-animation mode, so Settings knows whether to show its toggle. */
internal val LocalExpressiveAnimation = staticCompositionLocalOf { ExpressiveAnimation.ON }

/** Material theme driven by the user's appearance settings (color theme, palette, dark mode, motion). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun FoundationTheme(
    config: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val viewModel = foundationViewModel()
    val userExpressiveAnimation by viewModel.setting(FoundationSettingKeys.expressiveAnimation).collectAsState()
    val useExpressiveAnimation = when (config.expressiveAnimation) {
        ExpressiveAnimation.ON -> true
        ExpressiveAnimation.OFF -> false
        ExpressiveAnimation.USER_CHOICE -> userExpressiveAnimation
    }
    val useColorTheme by viewModel.setting(FoundationSettingKeys.colorTheme).collectAsState()
    val usePalette by viewModel.setting(FoundationSettingKeys.paletteStyle).collectAsState()
    val useDarkMode by viewModel.setting(FoundationSettingKeys.darkMode).collectAsState()
    
    val useAmoled = useDarkMode == DarkMode.AMOLED
    val darkMode = when (useDarkMode) {
        DarkMode.AUTO -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
        DarkMode.AMOLED -> true
    }
    
    val systemColorScheme = if (useColorTheme == ColorTheme.AUTO) systemDynamicColorScheme(darkMode) else null
    
    val colorScheme: ColorScheme = when {
        systemColorScheme != null && useAmoled ->
            systemColorScheme.copy(
                background = Color.Black,
                onBackground = Color.White,
                surface = Color.Black,
                onSurface = Color.White,
            )
        
        systemColorScheme != null -> systemColorScheme
        
        useColorTheme.isPreset ->
            rememberDynamicColorScheme(
                seedColor = config.presetColors[useColorTheme] ?: useColorTheme.color,
                isDark = darkMode,
                isAmoled = useAmoled,
                style = usePalette.paletteStyle
            )
        
        useColorTheme == ColorTheme.OFF ->
            rememberDynamicColorScheme(
                seedColor = useColorTheme.color,
                isDark = darkMode,
                isAmoled = useAmoled,
                style = PaletteStyle.Monochrome
            )
        
        darkMode -> DarkColorScheme
        else -> LightColorScheme
    }
    
    SystemBarsAppearance(darkMode)
    
    CompositionLocalProvider(LocalExpressiveAnimation provides config.expressiveAnimation) {
        MaterialTheme(
            colorScheme = animateColorScheme(colorScheme),
            motionScheme = if (useExpressiveAnimation) MotionScheme.expressive() else MotionScheme.standard(),
            typography = config.typography,
            content = content
        )
    }
}

/** The system's dynamic color scheme (Android 12+), or null where unsupported. */
@Composable
internal expect fun systemDynamicColorScheme(dark: Boolean): ColorScheme?

/** Light or dark system bar icons to match the theme. */
@Composable
internal expect fun SystemBarsAppearance(dark: Boolean)
