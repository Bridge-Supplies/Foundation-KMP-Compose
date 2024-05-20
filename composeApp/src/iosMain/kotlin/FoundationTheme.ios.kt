import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import bridge.supplies.foundation.DarkColorScheme
import bridge.supplies.foundation.LightColorScheme
import bridge.supplies.foundation.Typography

@Composable
actual fun FoundationTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean, // Android 12+
    content: @Composable () -> Unit
) {
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