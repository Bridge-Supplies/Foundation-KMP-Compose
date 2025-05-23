package ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import foundation.composeapp.generated.resources.app_settings_scroll_helpers_subtitle
import foundation.composeapp.generated.resources.app_settings_scroll_helpers_title
import foundation.composeapp.generated.resources.app_settings_title
import foundation.composeapp.generated.resources.app_settings_vibration_subtitle
import foundation.composeapp.generated.resources.app_settings_vibration_title
import foundation.composeapp.generated.resources.navigation_settings_about
import foundation.composeapp.generated.resources.theme_settings_color_theme_subtitle
import foundation.composeapp.generated.resources.theme_settings_color_theme_title
import foundation.composeapp.generated.resources.theme_settings_dark_mode_subtitle
import foundation.composeapp.generated.resources.theme_settings_dark_mode_title
import foundation.composeapp.generated.resources.theme_settings_palette_subtitle
import foundation.composeapp.generated.resources.theme_settings_palette_title
import foundation.composeapp.generated.resources.theme_settings_title
import org.jetbrains.compose.resources.stringResource
import ui.AppBarAction
import ui.BottomButton
import ui.EdgeFadeLazyStaggeredVerticalGrid
import ui.Screen
import ui.SettingsSelector
import ui.SettingsSwitch
import ui.TitledCard
import ui.rememberNestedScrollConnection

@Composable
fun SettingsOptionsScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val horPaddingMod = Modifier.padding(horizontal = 16.dp)
    val appBarAction by viewModel.activeAppBarAction.collectAsState()
    val listState = rememberLazyStaggeredGridState()
    
    var bottomButtonsVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(
        firstVisibleItemScrollOffset = { listState.firstVisibleItemScrollOffset }
    ) { scrollUp ->
        bottomButtonsVisible = scrollUp
    }
    
    Screen.SETTINGS_OPTIONS.actions.forEach { action ->
        if (appBarAction == action) {
            when (action) {
                AppBarAction.SETTINGS -> {
                    systemAppSettings()
                    viewModel.consumeAppBarAction()
                    hapticFeedback()
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
        EdgeFadeLazyStaggeredVerticalGrid(
            modifier = horPaddingMod
                .nestedScroll(nestedScrollConnection)
                .align(Alignment.Center),
            state = listState,
            columns = if (isPortraitMode) 1 else 2,
            verticalItemSpacing = 8.dp,
            horizontalItemSpacing = 16.dp
        ) {
            item {
                ThemeSettings(
                    modifier = Modifier.animateItem(),
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback
                )
            }
            
            item {
                GeneralSettings(
                    modifier = Modifier.animateItem(),
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback
                )
            }
            
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.minimumInteractiveComponentSize())
            }
        }
        
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            visible = bottomButtonsVisible,
            enter = slideInVertically(initialOffsetY = { it * 2 }),
            exit = slideOutVertically(targetOffsetY = { it * 2 }),
        ) {
            BottomButton(
                text = stringResource(Res.string.navigation_settings_about),
                paddingValues = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            ) {
                hapticFeedback()
                onNavigateToAbout()
            }
        }
    }
}

@Composable
fun GeneralSettings(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val useEncryptedShare by viewModel.useEncryptedShare.collectAsState()
    val useLandingTips by viewModel.useLandingTips.collectAsState()
    val useFullscreenLandscape by viewModel.useFullscreenLandscape.collectAsState()
    val useVibration by viewModel.useVibration.collectAsState()
    val useScrollHelpers by viewModel.useScrollHelpers.collectAsState()
    
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
            hapticFeedback()
        }
        
        SettingsSwitch(
            title = stringResource(Res.string.app_settings_landing_tips_title),
            subtitle = stringResource(Res.string.app_settings_landing_tips_subtitle),
            enabled = useLandingTips
        ) { enabled ->
            viewModel.useLandingTips(enabled)
            hapticFeedback()
        }
        
        if (viewModel.supportsFeature(Feature.FULLSCREEN_LANDSCAPE)) {
            SettingsSwitch(
                title = stringResource(Res.string.app_settings_fullscreen_landscape_title),
                subtitle = stringResource(Res.string.app_settings_fullscreen_landscape_subtitle),
                enabled = useFullscreenLandscape
            ) { enabled ->
                viewModel.useFullscreenLandscape(enabled)
                hapticFeedback()
            }
        }
        
        if (viewModel.supportsFeature(Feature.VIBRATION)) {
            SettingsSwitch(
                title = stringResource(Res.string.app_settings_vibration_title),
                subtitle = stringResource(Res.string.app_settings_vibration_subtitle),
                enabled = useVibration
            ) { enabled ->
                viewModel.useVibration(enabled)
                hapticFeedback()
            }
        }
        
        SettingsSwitch(
            title = stringResource(Res.string.app_settings_scroll_helpers_title),
            subtitle = stringResource(Res.string.app_settings_scroll_helpers_subtitle),
            enabled = useScrollHelpers
        ) { enabled ->
            viewModel.useScrollHelpers(enabled)
            hapticFeedback()
        }
        
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ThemeSettings(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
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
                hapticFeedback()
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
                    hapticFeedback()
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
                hapticFeedback()
            },
            optionName = { darkMode ->
                stringResource(darkMode.titleRes)
            }
        )
        
        Spacer(Modifier.height(8.dp))
    }
}