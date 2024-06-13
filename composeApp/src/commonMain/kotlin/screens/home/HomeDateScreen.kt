package screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.MainViewModel
import data.todaysDate
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_about_elapsed_seconds
import foundation.composeapp.generated.resources.app_about_today
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeDateScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val isPortraitMode = isPortraitMode()
    val timer by viewModel.timer.collectAsState()
    val todaysDate by remember { mutableStateOf(todaysDate()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceContainerColor)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val text = stringResource(Res.string.app_about_today, todaysDate) + "\n" +
            stringResource(Res.string.app_about_elapsed_seconds, timer)
        
        Text(
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = onSurfaceColor,
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}