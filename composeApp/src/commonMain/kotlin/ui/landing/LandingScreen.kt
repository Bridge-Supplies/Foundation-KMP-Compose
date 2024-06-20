package ui.landing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import config.ColorSchemeStyle
import config.getAppliedColorScheme
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.ic_launcher_foreground
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.TRANSITION_EASING
import ui.circularReveal

@Composable
fun LandingScreen(
    onLandingCompleted: suspend () -> Unit
) {
    val delayMs = 1500
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    var isRevealed by remember { mutableStateOf(false) }
    
    val animationProgress: State<Float> = animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = delayMs, easing = TRANSITION_EASING),
        label = "landing_reveal"
    )
    
    LaunchedEffect(Unit) {
        isRevealed = true
        delay(delayMs.toLong())
        onLandingCompleted()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .circularReveal(
                progress = animationProgress.value
            )
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
            style = MaterialTheme.typography.titleLarge,
            text = stringResource(Res.string.app_name)
        )
    }
}