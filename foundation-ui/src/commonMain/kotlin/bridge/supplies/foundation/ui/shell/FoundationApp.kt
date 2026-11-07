package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import bridge.supplies.foundation.ui.FoundationViewModel
import bridge.supplies.foundation.ui.components.ClickableIcon
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.isPortrait
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.navigation_back
import bridge.supplies.foundation.ui.resources.navigation_close
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.sheets.SheetContent
import bridge.supplies.foundation.ui.sheets.SheetHost
import bridge.supplies.foundation.ui.theme.FoundationTheme
import bridge.supplies.foundation.ui.theme.ThemeConfig
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** What [FoundationApp] shows before the tabs. */
public sealed interface Landing {
    public data object None : Landing

    /**
     * The app [icon] and name, with a random tip (Foundation's plus [extraTips]) when tips are
     * enabled.
     */
    public class Default(
        public val icon: DrawableResource,
        public val extraTips: List<StringResource> = emptyList(),
    ) : Landing

    /** App-drawn landing (e.g. onboarding). Call `onDone` to continue to the first tab. */
    public class Custom(public val content: @Composable (onDone: () -> Unit) -> Unit) : Landing
}

/** Shell layout options. Defaults follow the platform. */
public data class ShellOptions(
    /** Center the rail items vertically (Android/iOS) or align them to the top (desktop). */
    val railVerticallyCentered: Boolean = RAIL_VERTICALLY_CENTERED,
    /** Center the top bar title (iOS). */
    val centerTopBarTitle: Boolean = CENTER_TOP_BAR_TITLE,
)

public val LocalSnackbarHostState: ProvidableCompositionLocal<SnackbarHostState> =
    staticCompositionLocalOf {
        error("SnackbarHostState is only available inside FoundationApp")
    }

/**
 * The app shell: tabs with one back stack each, a top bar, and a bottom bar (portrait) or rail
 * (landscape).
 *
 * Applies [FoundationTheme] and reads Foundation's settings (fullscreen landscape, haptics) from
 * [FoundationViewModel].
 *
 * @param tabs top-level destinations, the first one is the start tab
 * @param routes serializers for every route used in [entries]
 * @param onShowSystemUi called with whether the system bars should show
 * @param onCloseApplication called on back from the start tab's root
 * @param sheets content for the app's own
 *   [SheetRequest][bridge.supplies.foundation.core.sheets.SheetRequest]s; Foundation draws the
 *   sheet
 * @param entries the app's screens: `entry<MyRoute>(metadata = screenMetadata(…)) { route -> … }`
 */
@Composable
public fun FoundationApp(
    tabs: List<FoundationTab>,
    routes: RouteRegistry,
    landing: Landing = Landing.None,
    theme: ThemeConfig = ThemeConfig(),
    shell: ShellOptions = ShellOptions(),
    onShowSystemUi: (Boolean) -> Unit = {},
    onCloseApplication: () -> Unit = {},
    sheets: SheetContent = {},
    entries: EntryProviderScope<NavKey>.() -> Unit,
) {
    FoundationTheme(theme) {
        FoundationShell(
            tabs,
            routes,
            landing,
            shell,
            onShowSystemUi,
            onCloseApplication,
            sheets,
            entries,
        )
    }
}

@Composable
private fun FoundationShell(
    tabs: List<FoundationTab>,
    routes: RouteRegistry,
    landing: Landing,
    shell: ShellOptions,
    onShowSystemUi: (Boolean) -> Unit,
    onCloseApplication: () -> Unit,
    sheets: SheetContent,
    entries: EntryProviderScope<NavKey>.() -> Unit,
) {
    val viewModel = foundationViewModel()
    val fullscreenLandscape by
        viewModel.setting(FoundationSettingKeys.fullscreenLandscape).collectAsState()
    val onHapticFeedback = rememberHapticFeedback()
    val navigator = rememberNavigator(tabs, routes)
    val snackbarHost = remember { SnackbarHostState() }
    val actionHost = remember { TopBarActionHost() }
    val backHost = remember { ScreenBackHost() }
    val tabStateHolder = rememberSaveableStateHolder()
    var landingDone by rememberSaveable { mutableStateOf(landing is Landing.None) }
    val density = LocalDensity.current

    // Each bar's size, measured while it shows. Every screen is padded for the bars its own chrome
    // shows, and the bars slide over the screens, so a screen never moves when the next one
    // shows other bars.
    val bars = remember { BarSizes() }

    val isPortrait = isPortrait()
    val provider = entryProvider(builder = entries)
    val entryFor: (NavKey) -> NavEntry<NavKey> = { route ->
        val entry = provider(route)
        NavEntry(route, entry.contentKey, entry.metadata) { _: NavKey ->
            val chrome = entry.metadata.screenChrome
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(
                            top = if (chrome.showsTopBar) bars.top else 0.dp,
                            bottom =
                                if (chrome.showsNavigation && isPortrait) bars.bottom else 0.dp,
                            start = if (chrome.showsNavigation && !isPortrait) bars.rail else 0.dp,
                        )
            ) {
                CompositionLocalProvider(LocalRoute provides route) { entry.Content() }
            }
        }
    }

    val metadata = provider(navigator.currentRoute).metadata
    val chrome = if (landingDone) metadata.screenChrome else Chrome.Immersive()
    val showTopBar = chrome.showsTopBar
    val showBottomBar = chrome.showsNavigation && isPortrait
    val showRail = chrome.showsNavigation && !isPortrait
    val hideSystemBars = (chrome as? Chrome.Immersive)?.hideSystemBars == true

    LaunchedEffect(isPortrait, fullscreenLandscape, hideSystemBars) {
        onShowSystemUi(!hideSystemBars && (isPortrait || !fullscreenLandscape))
    }

    val onSelectTab = { tab: FoundationTab ->
        if (tab != navigator.currentTab || navigator.canNavigateBack) onHapticFeedback()
        navigator.selectTab(tab.id)
    }
    // A screen's ScreenBackHandler comes first
    val onNavigateBack = {
        onHapticFeedback()
        val handler = backHost.handlers[navigator.currentRoute]
        if (handler != null) handler() else navigator.back()
    }

    // Tab roots: back to the first tab, then close. NavDisplay handles back within a tab.
    BackHandler(enabled = !landingDone || !navigator.canNavigateBack) {
        when {
            !landingDone -> onCloseApplication()
            navigator.currentTab != tabs.first() -> onNavigateBack()
            else -> onCloseApplication()
        }
    }

    // The top bar starts after the rail, following it as it slides
    val topBarStart by
        animateDpAsState(
            targetValue = if (showRail) bars.rail else 0.dp,
            animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING),
        )

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalSnackbarHostState provides snackbarHost,
        LocalTopBarActionHost provides actionHost,
        LocalScreenBackHost provides backHost,
        // Text and icons without their own color follow the theme, as on a Surface
        LocalContentColor provides MaterialTheme.colorScheme.onBackground,
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
        ) {
            AnimatedContent(
                targetState = if (landingDone) navigator.currentTabId else null,
                transitionSpec = {
                    TopLevelEnterTransition() togetherWith TopLevelExitTransition()
                },
                modifier = Modifier.fillMaxSize(),
            ) { tabId ->
                if (tabId == null) {
                    when (landing) {
                        is Landing.Default -> DefaultLandingScreen(landing) { landingDone = true }
                        is Landing.Custom -> landing.content { landingDone = true }
                        Landing.None -> Unit
                    }
                } else {
                    // Keeps each tab's screen state while another tab is shown
                    tabStateHolder.SaveableStateProvider(tabId) {
                        NavDisplay(
                            backStack = navigator.stackOf(tabId),
                            onBack = { onNavigateBack() },
                            transitionSpec = {
                                ScreenEnterTransition() togetherWith ScreenExitTransition()
                            },
                            popTransitionSpec = {
                                ScreenPopEnterTransition() togetherWith ScreenPopExitTransition()
                            },
                            predictivePopTransitionSpec = {
                                ScreenPopEnterTransition() togetherWith ScreenPopExitTransition()
                            },
                            entryProvider = entryFor,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showRail,
                enter = BarEnterTransition { -it },
                exit = BarExitTransition { -it },
                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight(),
            ) {
                ShellRail(
                    navigator = navigator,
                    verticallyCentered = shell.railVerticallyCentered,
                    onSelectTab = onSelectTab,
                    modifier =
                        Modifier.onSizeChanged {
                            bars.rail = with(density) { it.width.toDp() }
                        },
                )
            }

            AnimatedVisibility(
                visible = showTopBar,
                enter = BarEnterTransition(vertical = true) { -it },
                exit = BarExitTransition(vertical = true) { -it },
                modifier = Modifier.align(Alignment.TopStart).padding(start = topBarStart),
            ) {
                ShellTopBar(
                    navigator = navigator,
                    metadata = metadata,
                    actions = actionHost.actions[navigator.currentRoute].orEmpty(),
                    centerTitle = shell.centerTopBarTitle,
                    onNavigateBack = { onNavigateBack() },
                    modifier =
                        Modifier.onSizeChanged { bars.top = with(density) { it.height.toDp() } },
                )
            }

            AnimatedVisibility(
                visible = showBottomBar,
                enter = BarEnterTransition(vertical = true) { it },
                exit = BarExitTransition(vertical = true) { it },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            ) {
                BottomAppBar(
                    contentPadding = PaddingValues(0.dp),
                    modifier =
                        Modifier.onSizeChanged {
                            bars.bottom = with(density) { it.height.toDp() }
                        },
                ) {
                    ShellBottomBar(navigator = navigator, onSelectTab = onSelectTab)
                }
            }

            SnackbarHost(
                hostState = snackbarHost,
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = if (showBottomBar) bars.bottom else 0.dp),
            )

            SheetHost(sheets)
        }
    }
}

/** The bars' sizes, measured while they show; until then Material's defaults. */
private class BarSizes {
    var top by mutableStateOf(64.dp)
    var bottom by mutableStateOf(80.dp)
    var rail by mutableStateOf(80.dp)
}

private fun BarEnterTransition(
    vertical: Boolean = false,
    offset: (Int) -> Int,
): EnterTransition {
    val spec = tween<IntOffset>(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    val fade = fadeIn(tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING))
    return fade +
        if (vertical) slideInVertically(spec, offset) else slideInHorizontally(spec, offset)
}

private fun BarExitTransition(
    vertical: Boolean = false,
    offset: (Int) -> Int,
): ExitTransition {
    val spec = tween<IntOffset>(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    val fade = fadeOut(tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING))
    return fade +
        if (vertical) slideOutVertically(spec, offset) else slideOutHorizontally(spec, offset)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShellTopBar(
    navigator: Navigator,
    metadata: Map<String, Any>,
    actions: List<TopBarAction>,
    centerTitle: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        metadata.screenTitleText
            ?: metadata.screenTitle?.let { stringResource(it) }
            ?: stringResource(navigator.currentTab.title)

    val titleContent: @Composable () -> Unit = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    val navigationIcon: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = navigator.canNavigateBack,
            modifier = Modifier.fillMaxHeight(),
        ) {
            when (metadata.screenBackButton) {
                BackButton.Arrow ->
                    ClickableIcon(
                        onClick = onNavigateBack,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.navigation_back),
                    )
                BackButton.Close ->
                    ClickableIcon(
                        onClick = onNavigateBack,
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.navigation_close),
                    )
            }
        }
    }
    val actionsContent: @Composable RowScope.() -> Unit = {
        actions.forEach { action ->
            ClickableIcon(
                onClick = action.onClick,
                imageVector = action.icon,
                contentDescription = stringResource(action.label),
            )
        }
    }

    if (centerTitle) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actionsContent,
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actionsContent,
        )
    }
}

@Composable
private fun ShellRail(
    navigator: Navigator,
    verticallyCentered: Boolean,
    onSelectTab: (FoundationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier.fillMaxHeight()) {
        if (verticallyCentered) {
            Spacer(Modifier.weight(1f))
        } else {
            Spacer(Modifier.height(56.dp))
        }

        navigator.tabs.forEach { tab ->
            val selected = tab == navigator.currentTab

            NavigationRailItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = stringResource(tab.title),
                    )
                },
                label = { Text(stringResource(tab.title)) },
                selected = selected,
                onClick = { onSelectTab(tab) },
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ShellBottomBar(
    navigator: Navigator,
    onSelectTab: (FoundationTab) -> Unit,
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        navigator.tabs.forEach { tab ->
            val selected = tab == navigator.currentTab

            NavigationBarItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = stringResource(tab.title),
                    )
                },
                label = { Text(stringResource(tab.title)) },
                selected = selected,
                onClick = { onSelectTab(tab) },
            )
        }
    }
}
