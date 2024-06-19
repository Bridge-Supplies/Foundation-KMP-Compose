package ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.ColorSchemeStyle
import config.getAppliedColorScheme
import config.isPortraitMode
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_about_orientation
import foundation.composeapp.generated.resources.app_about_platform
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.ic_launcher_foreground
import foundation.composeapp.generated.resources.navigation_home_date
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeInfoScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit,
    onNavigateDateScreen: () -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val isPortraitMode = isPortraitMode()
    val orientationText = if (isPortraitMode) "Portrait" else "Landscape"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(
                resource = Res.drawable.ic_launcher_foreground
            ),
            colorFilter = ColorFilter.tint(colorScheme.onContentColor),
            contentDescription = stringResource(Res.string.app_name),
            modifier = Modifier
                .size(128.dp)
                .padding(8.dp)
        )
        
        val text = stringResource(Res.string.app_about_platform, viewModel.platform.name) + "\n" +
            stringResource(Res.string.app_about_orientation, orientationText)
        
        Text(
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = colorScheme.onContentColor,
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = {
                onNavigateDateScreen()
                onVibrate()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.buttonColor,
                contentColor = colorScheme.onButtonColor
            )
        ) {
            Text(stringResource(Res.string.navigation_home_date))
        }
    }
}
