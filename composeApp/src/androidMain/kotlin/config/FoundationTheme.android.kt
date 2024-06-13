package config

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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
import data.MainViewModel

@Composable
actual fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val useDarkTheme by viewModel.useDarkTheme.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    
    val darkTheme = when (useDarkTheme) {
        -1 -> isSystemInDarkTheme()
        0 -> false
        else -> true
    }
    
    val colorScheme =
        when {
            useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme)
                    dynamicDarkColorScheme(context)
                else
                    dynamicLightColorScheme(context)
            }
            
            darkTheme ->
                DarkColorScheme
            
            else ->
                LightColorScheme
        }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.apply {
                statusBarColor = colorScheme.surface.toArgb()
                navigationBarColor = colorScheme.secondaryContainer.toArgb()
                WindowCompat.getInsetsController(this, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}