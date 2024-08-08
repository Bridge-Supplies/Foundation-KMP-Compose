package ui.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.ColorSchemeStyle
import config.LANDING_SCREEN_LONG_DURATION_MS
import config.LANDING_SCREEN_REVEAL_DURATION_MS
import config.LANDING_SCREEN_SHORT_DURATION_MS
import config.getAppliedColorScheme
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.ic_launcher_foreground
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.CircularReveal

@Composable
fun LandingScreen(
    viewModel: MainViewModel,
    onLandingCompleted: suspend () -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val useLandingTips by viewModel.useLandingTips.collectAsState()
    val landingMessage by remember(useLandingTips) {
        if (useLandingTips) {
            mutableStateOf(viewModel.platform.getLandingTips().random())
        } else {
            mutableStateOf(Res.string.app_name)
        }
    }
    
    val landingDurationMs = if (useLandingTips) {
        LANDING_SCREEN_LONG_DURATION_MS
    } else {
        LANDING_SCREEN_SHORT_DURATION_MS
    }
    
    val showTip = suspend {
        delay(landingDurationMs.toLong())
        onLandingCompleted()
    }
    
    CircularReveal(
        startDelayMs = 100,
        revealDurationMs = LANDING_SCREEN_REVEAL_DURATION_MS,
        onCompleted = showTip
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.backgroundColor)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher_foreground),
                contentDescription = stringResource(Res.string.app_name),
                colorFilter = ColorFilter.tint(color = colorScheme.onBackgroundColor),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .width(200.dp)
                    .wrapContentHeight()
                    .padding(16.dp)
            )
            
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                text = stringResource(Res.string.app_name),
                textAlign = TextAlign.Center
            )
            
            if (useLandingTips) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(landingMessage),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}