package config

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import bridge.supplies.foundation.DarkColorScheme
import bridge.supplies.foundation.LightColorScheme
import bridge.supplies.foundation.Typography
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import data.MainViewModel

@Composable
actual fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val useColorTheme by viewModel.useColorTheme.collectAsState()
    val usePalette by viewModel.usePalette.collectAsState()
    val useDarkMode by viewModel.useDarkMode.collectAsState()
    
    val darkMode = when (useDarkMode) {
        DarkMode.AUTO -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    
    val colorScheme: ColorScheme = when {
        useColorTheme == ColorTheme.AUTO && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkMode)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }
        
        useColorTheme == ColorTheme.RED || useColorTheme == ColorTheme.GREEN || useColorTheme == ColorTheme.BLUE -> {
            rememberDynamicColorScheme(
                seedColor = useColorTheme.color,
                isDark = darkMode,
                style = usePalette.paletteStyle
            )
        }
        
        useColorTheme == ColorTheme.OFF -> {
            rememberDynamicColorScheme(
                seedColor = useColorTheme.color,
                isDark = darkMode,
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.apply {
                statusBarColor = colorScheme.surface.toArgb()
                navigationBarColor = colorScheme.secondaryContainer.toArgb()
                WindowCompat.getInsetsController(this, view).isAppearanceLightStatusBars = !darkMode
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}