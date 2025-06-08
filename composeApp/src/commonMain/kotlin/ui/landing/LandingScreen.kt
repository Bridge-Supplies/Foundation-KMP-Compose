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
import config.LANDING_SCREEN_LONG_DURATION_MS
import config.LANDING_SCREEN_REVEAL_DURATION_MS
import config.LANDING_SCREEN_SHORT_DURATION_MS
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.ic_launcher_foreground
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.BodyText
import ui.CircularReveal
import ui.TitleText

@Composable
fun LandingScreen(
    viewModel: MainViewModel,
    onLandingCompleted: suspend () -> Unit
) {
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher_foreground),
                contentDescription = stringResource(Res.string.app_name),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .width(200.dp)
                    .wrapContentHeight()
                    .padding(16.dp)
            )
            
            TitleText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = stringResource(Res.string.app_name),
                textAlign = TextAlign.Center
            )
            
            if (useLandingTips) {
                BodyText(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp),
                    text = stringResource(landingMessage),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}