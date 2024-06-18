package screens.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LandingScreen() {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
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