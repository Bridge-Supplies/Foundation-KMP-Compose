import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.theme_settings_auto
import foundation.composeapp.generated.resources.theme_settings_dark
import foundation.composeapp.generated.resources.theme_settings_dynamic_colors_title
import foundation.composeapp.generated.resources.theme_settings_light
import foundation.composeapp.generated.resources.theme_settings_title
import foundation.composeapp.generated.resources.theme_settings_vibration_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    haptics: HapticFeedback,
    isPortraitMode: Boolean
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val useDarkTheme by viewModel.useDarkTheme.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val useVibration by viewModel.useVibration.collectAsState()
    
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
        ThemeOptions(
            viewModel = viewModel,
            useDarkTheme = useDarkTheme,
            useDynamicColors = useDynamicColors,
            useVibration = useVibration,
            onUseDynamicColors = { enabled ->
                viewModel.useDynamicColors(enabled)
                viewModel.hapticFeedback(haptics)
            },
            onUseDarkTheme = { option ->
                viewModel.useDarkTheme(option)
                viewModel.hapticFeedback(haptics)
            },
            onUseVibration = { enabled ->
                viewModel.useVibration(enabled)
                viewModel.hapticFeedback(haptics)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeOptions(
    viewModel: MainViewModel,
    useDarkTheme: Int, // -1 = auto (system default), 0 = light theme, 1 = dark theme
    useDynamicColors: Boolean,
    useVibration: Boolean,
    onUseDynamicColors: (Boolean) -> Unit,
    onUseDarkTheme: (Int) -> Unit,
    onUseVibration: (Boolean) -> Unit,
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = primaryColor,
        checkedBorderColor = onPrimaryColor
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.theme_settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = onSurfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            listOf(-1, 0, 1).forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == useDarkTheme,
                    onClick = {
                        onUseDarkTheme(value)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                ) {
                    Text(
                        text = when (value) {
                            -1 -> stringResource(Res.string.theme_settings_auto)
                            0 -> stringResource(Res.string.theme_settings_light)
                            else -> stringResource(Res.string.theme_settings_dark)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        if (viewModel.supportsFeature(Feature.DYNAMIC_COLORS)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.theme_settings_dynamic_colors_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurfaceColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                
                Switch(
                    checked = useDynamicColors,
                    colors = switchColors,
                    onCheckedChange = {
                        onUseDynamicColors(it)
                    }
                )
            }
        }
        
        if (viewModel.supportsFeature(Feature.VIBRATION)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.theme_settings_vibration_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurfaceColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                
                Switch(
                    checked = useVibration,
                    colors = switchColors,
                    onCheckedChange = {
                        onUseVibration(it)
                    }
                )
            }
        }
    }
}