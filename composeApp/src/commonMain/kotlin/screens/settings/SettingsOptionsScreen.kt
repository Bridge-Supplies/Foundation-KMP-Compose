package screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import config.ColorSchemeStyle
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.getAppliedColorScheme
import config.isPortraitMode
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_settings_encryption_subtitle
import foundation.composeapp.generated.resources.app_settings_encryption_title
import foundation.composeapp.generated.resources.app_settings_title
import foundation.composeapp.generated.resources.navigation_settings_about
import foundation.composeapp.generated.resources.theme_settings_color_theme_subtitle
import foundation.composeapp.generated.resources.theme_settings_color_theme_subtitle_no_dynamic_colors
import foundation.composeapp.generated.resources.theme_settings_color_theme_title
import foundation.composeapp.generated.resources.theme_settings_dark_mode_subtitle
import foundation.composeapp.generated.resources.theme_settings_dark_mode_title
import foundation.composeapp.generated.resources.theme_settings_palette_subtitle
import foundation.composeapp.generated.resources.theme_settings_palette_title
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
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val isPortraitMode = isPortraitMode()
    val useEncryptedShare by viewModel.useEncryptedShare.collectAsState()
    val useColorTheme by viewModel.useColorTheme.collectAsState()
    val usePalette by viewModel.usePalette.collectAsState()
    val useDarkMode by viewModel.useDarkMode.collectAsState()
    val useVibration by viewModel.useVibration.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.contentColor)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GeneralSettings(
            viewModel = viewModel,
            useEncryptedShare = useEncryptedShare,
            useVibration = useVibration,
            onUseEncryptedShare = { enabled ->
                viewModel.useEncryptedShare(enabled)
                onVibrate()
            },
            onUseVibration = { enabled ->
                viewModel.useVibration(enabled)
                onVibrate()
            }
        )
        
        ThemeSettings(
            viewModel = viewModel,
            useColorTheme = useColorTheme,
            usePalette = usePalette,
            useDarkMode = useDarkMode,
            onUseColorTheme = { option ->
                viewModel.useColorTheme(option)
                onVibrate()
            },
            onUsePalette = { option ->
                viewModel.usePalette(option)
                onVibrate()
            },
            onUseDarkMode = { option ->
                viewModel.useDarkMode(option)
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
                containerColor = colorScheme.buttonColor,
                contentColor = colorScheme.onButtonColor
            )
        ) {
            Text(stringResource(Res.string.navigation_settings_about))
        }
    }
}

@Composable
fun GeneralSettings(
    viewModel: MainViewModel,
    useEncryptedShare: Boolean,
    useVibration: Boolean,
    onUseEncryptedShare: (Boolean) -> Unit,
    onUseVibration: (Boolean) -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val switchColors = SwitchDefaults.colors(
        checkedTrackColor = colorScheme.buttonColor,
        checkedBorderColor = colorScheme.onButtonColor
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
            color = colorScheme.onContentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 4.dp)
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
                    color = colorScheme.onContentColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
                
                Text(
                    text = stringResource(Res.string.app_settings_encryption_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onContentColor,
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
        
        if (viewModel.supportsFeature(Feature.VIBRATION)) {
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
                        color = colorScheme.onContentColor,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )
                    
                    Text(
                        text = stringResource(Res.string.theme_settings_vibration_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onContentColor,
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
    useColorTheme: ColorTheme,
    usePalette: Palette,
    useDarkMode: DarkMode,
    onUseColorTheme: (ColorTheme) -> Unit,
    onUsePalette: (Palette) -> Unit,
    onUseDarkMode: (DarkMode) -> Unit,
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(Res.string.theme_settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.onContentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        
        Text(
            text = stringResource(Res.string.theme_settings_color_theme_title),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onContentColor,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        
        Text(
            text = if (viewModel.supportsFeature(Feature.DYNAMIC_COLORS))
                stringResource(Res.string.theme_settings_color_theme_subtitle)
            else
                stringResource(Res.string.theme_settings_color_theme_subtitle_no_dynamic_colors),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onContentColor,
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
            val buttonList: List<ColorTheme> = if (viewModel.supportsFeature(Feature.DYNAMIC_COLORS)) {
                listOf(ColorTheme.AUTO, ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE, ColorTheme.OFF)
            } else {
                listOf(ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE, ColorTheme.OFF)
            }
            
            buttonList.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == useColorTheme,
                    onClick = {
                        onUseColorTheme(value)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = buttonList.size
                    ),
                ) {
                    Text(
                        text = stringResource(value.stringRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        AnimatedVisibility(useColorTheme == ColorTheme.RED || useColorTheme == ColorTheme.GREEN || useColorTheme == ColorTheme.BLUE) {
            Column {
                Text(
                    text = stringResource(Res.string.theme_settings_palette_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onContentColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                
                Text(
                    text = stringResource(Res.string.theme_settings_palette_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onContentColor,
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
                    Palette.entries.forEachIndexed { index, value ->
                        SegmentedButton(
                            selected = value == usePalette,
                            onClick = {
                                onUsePalette(value)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = Palette.entries.size),
                        ) {
                            Text(
                                text = stringResource(value.stringRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        Text(
            text = stringResource(Res.string.theme_settings_dark_mode_title),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onContentColor,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        
        Text(
            text = stringResource(Res.string.theme_settings_dark_mode_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onContentColor,
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
            DarkMode.entries.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == useDarkMode,
                    onClick = {
                        onUseDarkMode(value)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DarkMode.entries.size),
                ) {
                    Text(
                        text = stringResource(value.stringRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}