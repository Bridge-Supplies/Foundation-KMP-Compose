package bridge.supplies.foundation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.core.platform.openAppSettings
import bridge.supplies.foundation.core.settings.EnumSetting
import bridge.supplies.foundation.core.settings.SettingKey
import bridge.supplies.foundation.ui.components.BottomButton
import bridge.supplies.foundation.ui.components.EdgeFadeLazyStaggeredVerticalGrid
import bridge.supplies.foundation.ui.components.OptionDetailText
import bridge.supplies.foundation.ui.components.SettingsSelector
import bridge.supplies.foundation.ui.components.SettingsSwitch
import bridge.supplies.foundation.ui.components.TitledCard
import bridge.supplies.foundation.ui.components.rememberNestedScrollConnection
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.isPortrait
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.action_settings
import bridge.supplies.foundation.ui.resources.app_settings_encryption_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_encryption_title
import bridge.supplies.foundation.ui.resources.app_settings_expressive_animation_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_expressive_animation_title
import bridge.supplies.foundation.ui.resources.app_settings_fullscreen_landscape_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_fullscreen_landscape_title
import bridge.supplies.foundation.ui.resources.app_settings_landing_tips_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_landing_tips_title
import bridge.supplies.foundation.ui.resources.app_settings_scroll_helpers_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_scroll_helpers_title
import bridge.supplies.foundation.ui.resources.app_settings_title
import bridge.supplies.foundation.ui.resources.app_settings_vibration_subtitle
import bridge.supplies.foundation.ui.resources.app_settings_vibration_title
import bridge.supplies.foundation.ui.resources.navigation_settings_about
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_subtitle
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_subtitle_no_dynamic_colors
import bridge.supplies.foundation.ui.resources.theme_settings_color_theme_title
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_subtitle
import bridge.supplies.foundation.ui.resources.theme_settings_dark_mode_title
import bridge.supplies.foundation.ui.resources.theme_settings_palette_subtitle
import bridge.supplies.foundation.ui.resources.theme_settings_palette_title
import bridge.supplies.foundation.ui.resources.theme_settings_title
import bridge.supplies.foundation.ui.shell.LocalNavigator
import bridge.supplies.foundation.ui.shell.TopBarAction
import bridge.supplies.foundation.ui.shell.TopBarActions
import bridge.supplies.foundation.ui.theme.ColorTheme
import bridge.supplies.foundation.ui.theme.DarkMode
import bridge.supplies.foundation.ui.theme.ExpressiveAnimation
import bridge.supplies.foundation.ui.theme.LocalExpressiveAnimation
import bridge.supplies.foundation.ui.theme.Palette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Which of Foundation's built-in settings to show. Items the platform or [FeatureFlags] don't support are hidden anyway,
 * and the expressive-animation toggle only appears when `ThemeConfig.expressiveAnimation` is `USER_CHOICE`.
 */
public data class BuiltInSettings(
    val theme: Boolean = true,
    val encryptedSharing: Boolean = true,
    val landingTips: Boolean = true,
    val expressiveAnimation: Boolean = true,
    val fullscreenLandscape: Boolean = true,
    val vibration: Boolean = true,
    val scrollHelpers: Boolean = true,
)

@DslMarker
public annotation class SettingsDsl

/** App settings sections shown after Foundation's built-ins. */
@SettingsDsl
public interface SettingsScope {
    public fun section(title: StringResource, content: SettingsSectionScope.() -> Unit)
}

@SettingsDsl
public interface SettingsSectionScope {
    public fun switch(key: SettingKey<Boolean>, title: StringResource, subtitle: StringResource? = null)
    public fun <E : Enum<E>> choice(
        key: EnumSetting<E>,
        title: StringResource,
        subtitle: StringResource? = null,
        options: List<E> = key.entries,
        label: (E) -> StringResource,
    )

    /** A row that opens [route] on the current tab, e.g. a sub-screen of settings. */
    public fun link(title: StringResource, subtitle: StringResource? = null, route: NavKey)
    public fun custom(content: @Composable () -> Unit)
}

/**
 * Foundation's settings screen: Theming and System cards, then the app's [extra] sections, with an
 * "About app" button when [onNavigateToAbout] is set and a top bar shortcut to the system app settings.
 */
@Composable
public fun FoundationSettingsScreen(
    builtIns: BuiltInSettings = BuiltInSettings(),
    onNavigateToAbout: (() -> Unit)? = null,
    extra: SettingsScope.() -> Unit = { },
) {
    val hapticFeedback = rememberHapticFeedback()
    val isPortrait = isPortrait()
    val listState = rememberLazyStaggeredGridState()
    val sections = remember(extra) { SettingsBuilder().apply(extra).sections }

    var bottomButtonsVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(
        firstVisibleItemScrollOffset = { listState.firstVisibleItemScrollOffset }
    ) { scrollUp ->
        bottomButtonsVisible = scrollUp
    }

    TopBarActions(
        listOf(
            TopBarAction(Icons.Default.Settings, Res.string.action_settings) {
                openAppSettings()
                hapticFeedback()
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EdgeFadeLazyStaggeredVerticalGrid(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .nestedScroll(nestedScrollConnection),
            state = listState,
            columns = if (isPortrait) 1 else 2,
            horizontalItemSpacing = 0.dp,
            endSpacing = 56.dp
        ) {
            if (builtIns.theme) {
                item {
                    ThemeSettings(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .animateItem()
                    )
                }
            }

            item {
                SystemSettings(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItem(),
                    builtIns = builtIns
                )
            }

            sections.forEach { section ->
                item {
                    TitledCard(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .animateItem(),
                        title = stringResource(section.title)
                    ) {
                        section.items.forEach { it() }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        if (onNavigateToAbout != null) {
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
}

@Composable
private fun SystemSettings(
    modifier: Modifier = Modifier,
    builtIns: BuiltInSettings
) {
    val viewModel = foundationViewModel()
    val features = viewModel.config.features

    TitledCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = stringResource(Res.string.app_settings_title)
    ) {
        if (builtIns.encryptedSharing && features.qrSharing) {
            SettingSwitch(
                key = FoundationSettingKeys.encryptedShare,
                title = Res.string.app_settings_encryption_title,
                subtitle = Res.string.app_settings_encryption_subtitle
            )
        }

        if (builtIns.landingTips && features.landingTips) {
            SettingSwitch(
                key = FoundationSettingKeys.landingTips,
                title = Res.string.app_settings_landing_tips_title,
                subtitle = Res.string.app_settings_landing_tips_subtitle
            )
        }

        // Only when the theme leaves the choice to the user (ThemeConfig.expressiveAnimation = USER_CHOICE)
        if (builtIns.expressiveAnimation && LocalExpressiveAnimation.current == ExpressiveAnimation.USER_CHOICE) {
            SettingSwitch(
                key = FoundationSettingKeys.expressiveAnimation,
                title = Res.string.app_settings_expressive_animation_title,
                subtitle = Res.string.app_settings_expressive_animation_subtitle
            )
        }

        if (builtIns.fullscreenLandscape && viewModel.supports(PlatformFeature.FULLSCREEN_LANDSCAPE)) {
            SettingSwitch(
                key = FoundationSettingKeys.fullscreenLandscape,
                title = Res.string.app_settings_fullscreen_landscape_title,
                subtitle = Res.string.app_settings_fullscreen_landscape_subtitle
            )
        }

        if (builtIns.vibration && viewModel.supports(PlatformFeature.VIBRATION)) {
            SettingSwitch(
                key = FoundationSettingKeys.vibration,
                title = Res.string.app_settings_vibration_title,
                subtitle = Res.string.app_settings_vibration_subtitle
            )
        }

        if (builtIns.scrollHelpers) {
            SettingSwitch(
                key = FoundationSettingKeys.scrollHelpers,
                title = Res.string.app_settings_scroll_helpers_title,
                subtitle = Res.string.app_settings_scroll_helpers_subtitle
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ThemeSettings(
    modifier: Modifier = Modifier
) {
    val viewModel = foundationViewModel()
    val useColorTheme by viewModel.setting(FoundationSettingKeys.colorTheme).collectAsState()
    val supportsDynamicColors = viewModel.supports(PlatformFeature.DYNAMIC_COLORS)

    TitledCard(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = stringResource(Res.string.theme_settings_title)
    ) {
        SettingChoice(
            modifier = Modifier.padding(bottom = 8.dp),
            key = FoundationSettingKeys.colorTheme,
            title = Res.string.theme_settings_color_theme_title,
            subtitle = if (supportsDynamicColors) {
                Res.string.theme_settings_color_theme_subtitle
            } else {
                Res.string.theme_settings_color_theme_subtitle_no_dynamic_colors
            },
            options = if (supportsDynamicColors) ColorTheme.entries else ColorTheme.entries - ColorTheme.AUTO,
            label = { it.titleRes }
        )

        AnimatedVisibility(useColorTheme.isPreset) {
            SettingChoice(
                modifier = Modifier.padding(bottom = 8.dp),
                key = FoundationSettingKeys.paletteStyle,
                title = Res.string.theme_settings_palette_title,
                subtitle = Res.string.theme_settings_palette_subtitle,
                options = Palette.entries,
                label = { it.titleRes }
            )
        }

        SettingChoice(
            modifier = Modifier.padding(bottom = 8.dp),
            key = FoundationSettingKeys.darkMode,
            title = Res.string.theme_settings_dark_mode_title,
            subtitle = Res.string.theme_settings_dark_mode_subtitle,
            options = DarkMode.entries,
            label = { it.titleRes }
        )

        Spacer(Modifier.height(8.dp))
    }
}


// Building blocks bound to a setting key

/** [SettingsSwitch] that reads and writes [key]. */
@Composable
public fun SettingSwitch(
    key: SettingKey<Boolean>,
    title: StringResource,
    subtitle: StringResource? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel = foundationViewModel()
    val hapticFeedback = rememberHapticFeedback()
    val enabled by viewModel.setting(key).collectAsState()

    SettingsSwitch(
        modifier = modifier,
        title = stringResource(title),
        subtitle = subtitle?.let { stringResource(it) },
        enabled = enabled
    ) { value ->
        viewModel.set(key, value)
        hapticFeedback()
    }
}

/** [SettingsSelector] that reads and writes [key]. */
@Composable
public fun <E : Enum<E>> SettingChoice(
    key: EnumSetting<E>,
    title: StringResource,
    subtitle: StringResource? = null,
    options: List<E> = key.entries,
    modifier: Modifier = Modifier,
    label: (E) -> StringResource,
) {
    val viewModel = foundationViewModel()
    val hapticFeedback = rememberHapticFeedback()
    val selected by viewModel.setting(key).collectAsState()

    SettingsSelector(
        modifier = modifier,
        title = stringResource(title),
        subtitle = subtitle?.let { stringResource(it) },
        optionList = options,
        selectedOption = selected,
        onSelectOption = { option ->
            viewModel.set(key, option)
            hapticFeedback()
        },
        optionName = { stringResource(label(it)) }
    )
}

@Composable
private fun SettingLink(
    title: StringResource,
    subtitle: StringResource?,
    route: NavKey,
) {
    val navigator = LocalNavigator.current
    val hapticFeedback = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback()
                navigator.navigate(route)
            }
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionDetailText(
            modifier = Modifier
                .weight(1f)
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                ),
            title = stringResource(title),
            subtitle = subtitle?.let { stringResource(it) }
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null
        )
    }
}


// DSL implementation

private class SettingsSection(
    val title: StringResource,
    val items: List<@Composable () -> Unit>,
)

private class SettingsBuilder : SettingsScope {
    val sections = mutableListOf<SettingsSection>()

    override fun section(title: StringResource, content: SettingsSectionScope.() -> Unit) {
        sections += SettingsSection(title, SectionBuilder().apply(content).items)
    }
}

private class SectionBuilder : SettingsSectionScope {
    val items = mutableListOf<@Composable () -> Unit>()

    override fun switch(key: SettingKey<Boolean>, title: StringResource, subtitle: StringResource?) {
        items += { SettingSwitch(key, title, subtitle) }
    }

    override fun <E : Enum<E>> choice(
        key: EnumSetting<E>,
        title: StringResource,
        subtitle: StringResource?,
        options: List<E>,
        label: (E) -> StringResource,
    ) {
        items += { SettingChoice(key, title, subtitle, options, Modifier.padding(bottom = 8.dp), label) }
    }

    override fun link(title: StringResource, subtitle: StringResource?, route: NavKey) {
        items += { SettingLink(title, subtitle, route) }
    }

    override fun custom(content: @Composable () -> Unit) {
        items += content
    }
}
