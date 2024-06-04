import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import bridge.supplies.foundation.DarkColorScheme
import bridge.supplies.foundation.LightColorScheme
import bridge.supplies.foundation.Typography

@Composable
actual fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val useDarkTheme by viewModel.useDarkTheme.collectAsState()
    
    val darkTheme = when (useDarkTheme) {
        -1 -> isSystemInDarkTheme()
        0 -> false
        else -> true
    }
    
    val colorScheme =
        when {
            darkTheme ->
                DarkColorScheme
            
            else ->
                LightColorScheme
        }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}