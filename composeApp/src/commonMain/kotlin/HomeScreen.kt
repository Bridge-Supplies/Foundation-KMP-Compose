import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.ic_launcher_foreground
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    haptics: HapticFeedback,
    isPortraitMode: Boolean
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val timer by viewModel.timer.collectAsState()
    val todaysDate by remember { mutableStateOf(todaysDate()) }
    val orientationText = if (isPortraitMode) "Portrait" else "Landscape"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceContainerColor)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else 64.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(
                resource = Res.drawable.ic_launcher_foreground
            ),
            colorFilter = ColorFilter.tint(onSurfaceColor),
            contentDescription = "Foundation",
            modifier = Modifier
                .size(128.dp)
                .padding(8.dp)
        )
        
        Text(
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = onSurfaceColor,
            text = "Platform: ${viewModel.platform.name}" +
                "\nOrientation: $orientationText" +
                "\nApp version: ${viewModel.platform.version}" +
                "\nApp build: ${viewModel.platform.build}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Text(
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = onSurfaceColor,
            text = "\nToday's date: $todaysDate" +
                "\nElapsed seconds: $timer",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}
