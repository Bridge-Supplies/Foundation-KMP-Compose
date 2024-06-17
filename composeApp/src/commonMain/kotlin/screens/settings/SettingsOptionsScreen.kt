package screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import config.Feature
import config.isPortraitMode
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_settings_encryption_subtitle
import foundation.composeapp.generated.resources.app_settings_encryption_title
import foundation.composeapp.generated.resources.app_settings_title
import foundation.composeapp.generated.resources.navigation_settings_about
import foundation.composeapp.generated.resources.system_settings_title
import foundation.composeapp.generated.resources.theme_settings_auto
import foundation.composeapp.generated.resources.theme_settings_dark
import foundation.composeapp.generated.resources.theme_settings_dark_mode_subtitle
import foundation.composeapp.generated.resources.theme_settings_dark_mode_title
import foundation.composeapp.generated.resources.theme_settings_dynamic_colors_subtitle
import foundation.composeapp.generated.resources.theme_settings_dynamic_colors_title
import foundation.composeapp.generated.resources.theme_settings_light
import foundation.composeapp.generated.resources.theme_settings_title
import foundation.composeapp.generated.resources.theme_settings_vibration_subtitle
import foundation.composeapp.generated.resources.theme_settings_vibration_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsOptionsScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val isPortraitMode = isPortraitMode()
    val useEncryptedShare by viewModel.useEncryptedShare.collectAsState()
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
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppSettings(
            viewModel = viewModel,
            useEncryptedShare = useEncryptedShare,
            onUseEncryptedShare = { enabled ->
                viewModel.useEncryptedShare(enabled)
                onVibrate()
            }
        )
        
        SystemSettings(
            viewModel = viewModel,
            useVibration = useVibration,
            onUseVibration = { enabled ->
                viewModel.useVibration(enabled)
                onVibrate()
            }
        )
        
        ThemeSettings(
            viewModel = viewModel,
            useDarkTheme = useDarkTheme,
            useDynamicColors = useDynamicColors,
            onUseDynamicColors = { enabled ->
                viewModel.useDynamicColors(enabled)
                onVibrate()
            },
            onUseDarkTheme = { option ->
                viewModel.useDarkTheme(option)
                onVibrate()
            }
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = {
                onNavigateToAbout()
                onVibrate()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = onPrimaryColor
            )
        ) {
            Text(stringResource(Res.string.navigation_settings_about))
        }
    }
}

@Composable
fun AppSettings(
    viewModel: MainViewModel,
    useEncryptedShare: Boolean,
    onUseEncryptedShare: (Boolean) -> Unit
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
            .wrapContentHeight()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(Res.string.app_settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = onSurfaceColor,
            modifier = Modifier
                .fillMaxWidth()
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.app_settings_encryption_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
                
                Text(
                    text = stringResource(Res.string.app_settings_encryption_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
            }
            
            Switch(
                checked = useEncryptedShare,
                colors = switchColors,
                onCheckedChange = {
                    onUseEncryptedShare(it)
                }
            )
        }
    }
}

@Composable
fun SystemSettings(
    viewModel: MainViewModel,
    useVibration: Boolean,
    onUseVibration: (Boolean) -> Unit
) {
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = primaryColor,
        checkedBorderColor = onPrimaryColor
    )
    
    if (viewModel.supportsFeature(Feature.VIBRATION)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.system_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = onSurfaceColor,
                modifier = Modifier
                    .fillMaxWidth()
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.theme_settings_vibration_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurfaceColor,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                    
                    Text(
                        text = stringResource(Res.string.theme_settings_vibration_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                }
                
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettings(
    viewModel: MainViewModel,
    useDarkTheme: Int, // -1 = auto (system default), 0 = light theme, 1 = dark theme
    useDynamicColors: Boolean,
    onUseDynamicColors: (Boolean) -> Unit,
    onUseDarkTheme: (Int) -> Unit,
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
            .wrapContentHeight()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(Res.string.theme_settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = onSurfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        Text(
            text = stringResource(Res.string.theme_settings_dark_mode_title),
            style = MaterialTheme.typography.bodyLarge,
            color = onSurfaceColor,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        )
        
        Text(
            text = stringResource(Res.string.theme_settings_dark_mode_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceColor,
            modifier = Modifier
                .wrapContentHeight()
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
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.theme_settings_dynamic_colors_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = onSurfaceColor,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                    
                    Text(
                        text = stringResource(Res.string.theme_settings_dynamic_colors_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                }
                
                Switch(
                    checked = useDynamicColors,
                    colors = switchColors,
                    onCheckedChange = {
                        onUseDynamicColors(it)
                    }
                )
            }
        }
    }
}