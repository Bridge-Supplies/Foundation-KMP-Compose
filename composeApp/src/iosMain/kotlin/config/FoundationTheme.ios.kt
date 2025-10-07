package config

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.materialkolor.PaletteStyle
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.rememberDynamicColorScheme
import data.MainViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val useColorTheme by viewModel.useColorTheme.collectAsState()
    val usePalette by viewModel.usePalette.collectAsState()
    val useDarkMode by viewModel.useDarkMode.collectAsState()
    
    val useAmoled = useDarkMode == DarkMode.AMOLED
    val darkMode = when (useDarkMode) {
        DarkMode.AUTO -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
        DarkMode.AMOLED -> true
    }
    
    val colorScheme: ColorScheme = when (useColorTheme) {
        ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE -> {
            rememberDynamicColorScheme(
                seedColor = useColorTheme.color,
                isDark = darkMode,
                isAmoled = useAmoled,
                style = usePalette.paletteStyle
            )
        }
        
        ColorTheme.OFF -> {
            rememberDynamicColorScheme(
                seedColor = useColorTheme.color,
                isDark = darkMode,
                isAmoled = useAmoled,
                style = PaletteStyle.Monochrome
            )
        }
        
        else -> {
            if (darkMode)
                DarkColorScheme
            else
                LightColorScheme
        }
    }
    
    MaterialTheme(
        colorScheme = animateColorScheme(colorScheme),
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = content
    )
}