package bridge.supplies.foundation.ui.shell

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.ui.FoundationViewModel
import bridge.supplies.foundation.ui.components.BodyText
import bridge.supplies.foundation.ui.components.CircularReveal
import bridge.supplies.foundation.ui.components.TitleText
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.landing_tip_code_scanning
import bridge.supplies.foundation.ui.resources.landing_tip_dark_mode
import bridge.supplies.foundation.ui.resources.landing_tip_dynamic_colors
import bridge.supplies.foundation.ui.resources.landing_tip_fullscreen_landscape
import bridge.supplies.foundation.ui.resources.landing_tip_theme_colors
import bridge.supplies.foundation.ui.resources.landing_tip_vibration
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val LANDING_SCREEN_REVEAL_DURATION_MS = 1000
private const val LANDING_SCREEN_SHORT_DURATION_MS = 0
private const val LANDING_SCREEN_LONG_DURATION_MS = 2000

/** Foundation's tips plus the app's, limited to what this device and app support. */
internal fun FoundationViewModel.landingTips(extraTips: List<StringResource>): List<StringResource> = buildList {
    add(Res.string.landing_tip_dark_mode)
    add(Res.string.landing_tip_theme_colors)
    if (supports(PlatformFeature.FULLSCREEN_LANDSCAPE)) add(Res.string.landing_tip_fullscreen_landscape)
    if (supports(PlatformFeature.DYNAMIC_COLORS)) add(Res.string.landing_tip_dynamic_colors)
    if (supports(PlatformFeature.VIBRATION)) add(Res.string.landing_tip_vibration)
    if (supports(PlatformFeature.CODE_SCANNING) && config.features.qrSharing) add(Res.string.landing_tip_code_scanning)
    addAll(extraTips)
}

/** The app icon and name revealed in a circle, with a random tip when tips are enabled. */
@Composable
internal fun DefaultLandingScreen(
    landing: Landing.Default,
    onLandingCompleted: () -> Unit
) {
    val viewModel = foundationViewModel()
    val tipsSetting by viewModel.setting(FoundationSettingKeys.landingTips).collectAsState()
    val useLandingTips = tipsSetting && viewModel.config.features.landingTips
    val landingTip = remember(useLandingTips) {
        if (useLandingTips) viewModel.landingTips(landing.extraTips).random() else null
    }
    
    val landingDurationMs = if (useLandingTips) {
        LANDING_SCREEN_LONG_DURATION_MS
    } else {
        LANDING_SCREEN_SHORT_DURATION_MS
    }
    
    CircularReveal(
        startDelayMs = 100,
        revealDurationMs = LANDING_SCREEN_REVEAL_DURATION_MS,
        onCompleted = {
            delay(landingDurationMs.toLong())
            onLandingCompleted()
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Image(
                painter = painterResource(landing.icon),
                contentDescription = viewModel.config.appName,
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
                text = viewModel.config.appName,
                textAlign = TextAlign.Center
            )
            
            if (landingTip != null) {
                BodyText(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp),
                    text = stringResource(landingTip),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
