package ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.isPortraitMode
import data.MainViewModel
import data.systemAppSettings
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_settings_encryption_subtitle
import foundation.composeapp.generated.resources.app_settings_encryption_title
import foundation.composeapp.generated.resources.app_settings_fullscreen_landscape_subtitle
import foundation.composeapp.generated.resources.app_settings_fullscreen_landscape_title
import foundation.composeapp.generated.resources.app_settings_landing_tips_subtitle
import foundation.composeapp.generated.resources.app_settings_landing_tips_title
import foundation.composeapp.generated.resources.app_settings_title
import foundation.composeapp.generated.resources.navigation_settings_about
import foundation.composeapp.generated.resources.theme_settings_color_theme_subtitle
import foundation.composeapp.generated.resources.theme_settings_color_theme_title
import foundation.composeapp.generated.resources.theme_settings_dark_mode_subtitle
import foundation.composeapp.generated.resources.theme_settings_dark_mode_title
import foundation.composeapp.generated.resources.theme_settings_palette_subtitle
import foundation.composeapp.generated.resources.theme_settings_palette_title
import foundation.composeapp.generated.resources.theme_settings_title
import foundation.composeapp.generated.resources.theme_settings_vibration_subtitle
import foundation.composeapp.generated.resources.theme_settings_vibration_title
import org.jetbrains.compose.resources.stringResource
import ui.AppBarAction
import ui.BottomButton
import ui.EdgeFadeColumn
import ui.Screen
import ui.SettingsSelector
import ui.SettingsSwitch
import ui.TitledCard

@Composable
fun SettingsOptionsScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val horizontalPadding = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
    val appBarAction by viewModel.activeAppBarAction.collectAsState()
    val scrollState = rememberScrollState()
    
    Screen.SETTINGS_OPTIONS.actions.forEach { action ->
        if (appBarAction == action) {
            when (action) {
                AppBarAction.SETTINGS -> {
                    systemAppSettings()
                    viewModel.consumeAppBarAction()
                    onVibrate()
                }
                
                else -> {}
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EdgeFadeColumn(
            modifier = Modifier
                .padding(horizontal = horizontalPadding),
            state = scrollState,
            verticalItemSpacing = 8.dp
        ) {
            if (isPortraitMode) {
                GeneralSettings(
                    viewModel = viewModel,
                    onVibrate = onVibrate
                )
                
                ThemeSettings(
                    viewModel = viewModel,
                    onVibrate = onVibrate
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GeneralSettings(
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel,
                        onVibrate = onVibrate
                    )
                    
                    ThemeSettings(
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel,
                        onVibrate = onVibrate
                    )
                }
            }
            
            Spacer(Modifier.height(56.dp))
        }
        
        BottomButton(
            text = stringResource(Res.string.navigation_settings_about),
            paddingValues = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                bottom = 16.dp
            )
        ) {
            onVibrate()
            onNavigateToAbout()
        }
    }
}

@Composable
fun GeneralSettings(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val useEncryptedShare by viewModel.useEncryptedShare.collectAsState()
    val useLandingTips by viewModel.useLandingTips.collectAsState()
    val useFullscreenLandscape by viewModel.useFullscreenLandscape.collectAsState()
    val useVibration by viewModel.useVibration.collectAsState()
    
    TitledCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = stringResource(Res.string.app_settings_title)
    ) {
        SettingsSwitch(
            title = stringResource(Res.string.app_settings_encryption_title),
            subtitle = stringResource(Res.string.app_settings_encryption_subtitle),
            enabled = useEncryptedShare
        ) { enabled ->
            viewModel.useEncryptedShare(enabled)
            onVibrate()
        }
        
        SettingsSwitch(
            title = stringResource(Res.string.app_settings_landing_tips_title),
            subtitle = stringResource(Res.string.app_settings_landing_tips_subtitle),
            enabled = useLandingTips
        ) { enabled ->
            viewModel.useLandingTips(enabled)
            onVibrate()
        }
        
        if (viewModel.supportsFeature(Feature.FULLSCREEN_LANDSCAPE)) {
            SettingsSwitch(
                title = stringResource(Res.string.app_settings_fullscreen_landscape_title),
                subtitle = stringResource(Res.string.app_settings_fullscreen_landscape_subtitle),
                enabled = useFullscreenLandscape
            ) { enabled ->
                viewModel.useFullscreenLandscape(enabled)
                onVibrate()
            }
        }
        
        if (viewModel.supportsFeature(Feature.VIBRATION)) {
            SettingsSwitch(
                title = stringResource(Res.string.theme_settings_vibration_title),
                subtitle = stringResource(Res.string.theme_settings_vibration_subtitle),
                enabled = useVibration
            ) { enabled ->
                viewModel.useVibration(enabled)
                onVibrate()
            }
        }
    }
}

@Composable
fun ThemeSettings(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val useColorTheme by viewModel.useColorTheme.collectAsState()
    val usePalette by viewModel.usePalette.collectAsState()
    val useDarkMode by viewModel.useDarkMode.collectAsState()
    
    TitledCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = stringResource(Res.string.theme_settings_title)
    ) {
        val buttonList: List<ColorTheme> = if (viewModel.supportsFeature(Feature.DYNAMIC_COLORS)) {
            listOf(ColorTheme.AUTO, ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE, ColorTheme.OFF)
        } else {
            listOf(ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE, ColorTheme.OFF)
        }
        
        SettingsSelector(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.theme_settings_color_theme_title),
            subtitle = stringResource(Res.string.theme_settings_color_theme_subtitle),
            optionList = buttonList,
            selectedOption = useColorTheme,
            onSelectOption = { colorTheme ->
                viewModel.useColorTheme(colorTheme)
                onVibrate()
            },
            optionName = { colorTheme ->
                stringResource(colorTheme.titleRes)
            }
        )
        
        AnimatedVisibility(listOf(ColorTheme.RED, ColorTheme.GREEN, ColorTheme.BLUE).contains(useColorTheme)) {
            SettingsSelector(
                modifier = Modifier.padding(bottom = 8.dp),
                title = stringResource(Res.string.theme_settings_palette_title),
                subtitle = stringResource(Res.string.theme_settings_palette_subtitle),
                optionList = Palette.entries,
                selectedOption = usePalette,
                onSelectOption = { palette ->
                    viewModel.usePalette(palette)
                    onVibrate()
                },
                optionName = { palette ->
                    stringResource(palette.titleRes)
                }
            )
        }
        
        SettingsSelector(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.theme_settings_dark_mode_title),
            subtitle = stringResource(Res.string.theme_settings_dark_mode_subtitle),
            optionList = DarkMode.entries,
            selectedOption = useDarkMode,
            onSelectOption = { darkMode ->
                viewModel.useDarkMode(darkMode)
                onVibrate()
            },
            optionName = { darkMode ->
                stringResource(darkMode.titleRes)
            }
        )
    }
}