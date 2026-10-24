package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import bridge.supplies.foundation.ui.components.ClickableIcon
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.navigation_back
import org.jetbrains.compose.resources.stringResource

/** What [FoundationApp] shows before the tabs. */
public sealed interface Landing {
    public data object None : Landing

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
    staticCompositionLocalOf { error("SnackbarHostState is only available inside FoundationApp") }

/**
 * The app shell: tabs with one back stack each, a top bar, and a bottom bar (portrait) or rail (landscape).
 *
 * @param tabs top-level destinations, the first one is the start tab
 * @param routes serializers for every route used in [entries]
 * @param fullscreenLandscape hide the system bars in landscape
 * @param onShowSystemUi called with whether the system bars should show
 * @param onCloseApplication called on back from the start tab's root
 * @param onHapticFeedback called on tab selection and back navigation
 * @param overlay drawn above the screens, e.g. bottom sheets
 * @param entries the app's screens: `entry<MyRoute>(metadata = screenMetadata(…)) { route -> … }`
 */
@Composable
public fun FoundationApp(
    tabs: List<FoundationTab>,
    routes: RouteRegistry,
    landing: Landing = Landing.None,
    shell: ShellOptions = ShellOptions(),
    fullscreenLandscape: Boolean = false,
    onShowSystemUi: (Boolean) -> Unit = { },
    onCloseApplication: () -> Unit = { },
    onHapticFeedback: () -> Unit = { },
    overlay: @Composable () -> Unit = { },
    entries: EntryProviderScope<NavKey>.() -> Unit,
) {
    val navigator = rememberNavigator(tabs, routes)
    val snackbarHost = remember { SnackbarHostState() }
    val actionHost = remember { TopBarActionHost() }
    val tabStateHolder = rememberSaveableStateHolder()
    var landingDone by rememberSaveable { mutableStateOf(landing is Landing.None) }

    val provider = entryProvider(builder = entries)
    val entryFor: (NavKey) -> NavEntry<NavKey> = { route ->
        val entry = provider(route)
        NavEntry(route, entry.contentKey, entry.metadata) { _: NavKey ->
            CompositionLocalProvider(LocalRoute provides route) { entry.Content() }
        }
    }

    val isPortrait = isPortraitWindow()
    val metadata = provider(navigator.currentRoute).metadata
    val chrome = if (landingDone) metadata.screenChrome else Chrome.Immersive()
    val showBars = chrome is Chrome.Standard
    val hideSystemBars = (chrome as? Chrome.Immersive)?.hideSystemBars == true

    LaunchedEffect(isPortrait, fullscreenLandscape, hideSystemBars) {
        onShowSystemUi(!hideSystemBars && (isPortrait || !fullscreenLandscape))
    }

    val onSelectTab = { tab: FoundationTab ->
        if (tab != navigator.currentTab || navigator.canNavigateBack) onHapticFeedback()
        navigator.selectTab(tab.id)
    }
    val onNavigateBack = {
        onHapticFeedback()
        navigator.back()
    }

    // Tab roots: back to the first tab, then close. NavDisplay handles back within a tab.
    BackHandler(enabled = !landingDone || !navigator.canNavigateBack) {
        when {
            !landingDone -> onCloseApplication()
            navigator.currentTab != tabs.first() -> onNavigateBack()
            else -> onCloseApplication()
        }
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalSnackbarHostState provides snackbarHost,
        LocalTopBarActionHost provides actionHost,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
        ) {
            AnimatedVisibility(
                visible = showBars && !isPortrait,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                ShellRail(
                    navigator = navigator,
                    verticallyCentered = shell.railVerticallyCentered,
                    onSelectTab = onSelectTab
                )
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    AnimatedVisibility(
                        visible = showBars,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        ShellTopBar(
                            navigator = navigator,
                            metadata = metadata,
                            actions = actionHost.actions[navigator.currentRoute].orEmpty(),
                            centerTitle = shell.centerTopBarTitle,
                            onNavigateBack = { onNavigateBack() }
                        )
                    }
                },
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBars && isPortrait,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BottomAppBar(
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            ShellBottomBar(
                                navigator = navigator,
                                onSelectTab = onSelectTab
                            )
                        }
                    }
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHost)
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = if (landingDone) navigator.currentTabId else null,
                    transitionSpec = { TopLevelEnterTransition() togetherWith TopLevelExitTransition() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { tabId ->
                    if (tabId == null) {
                        (landing as? Landing.Custom)?.content?.invoke { landingDone = true }
                    } else {
                        // Keeps each tab's screen state while another tab is shown
                        tabStateHolder.SaveableStateProvider(tabId) {
                            NavDisplay(
                                backStack = navigator.stackOf(tabId),
                                onBack = { onNavigateBack() },
                                transitionSpec = { ScreenEnterTransition() togetherWith ScreenExitTransition() },
                                popTransitionSpec = { ScreenPopEnterTransition() togetherWith ScreenPopExitTransition() },
                                predictivePopTransitionSpec = { ScreenPopEnterTransition() togetherWith ScreenPopExitTransition() },
                                entryProvider = entryFor
                            )
                        }
                    }
                }

                overlay()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShellTopBar(
    navigator: Navigator,
    metadata: Map<String, Any>,
    actions: List<TopBarAction>,
    centerTitle: Boolean,
    onNavigateBack: () -> Unit
) {
    val title = metadata.screenTitleText
        ?: metadata.screenTitle?.let { stringResource(it) }
        ?: stringResource(navigator.currentTab.title)

    val titleContent: @Composable () -> Unit = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    val navigationIcon: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = navigator.canNavigateBack,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            ClickableIcon(
                onClick = onNavigateBack,
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.navigation_back)
            )
        }
    }
    val actionsContent: @Composable RowScope.() -> Unit = {
        actions.forEach { action ->
            ClickableIcon(
                onClick = action.onClick,
                imageVector = action.icon,
                contentDescription = stringResource(action.label)
            )
        }
    }

    if (centerTitle) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actionsContent
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actionsContent
        )
    }
}

@Composable
private fun ShellRail(
    navigator: Navigator,
    verticallyCentered: Boolean,
    onSelectTab: (FoundationTab) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
    ) {
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
                        contentDescription = stringResource(tab.title)
                    )
                },
                label = { Text(stringResource(tab.title)) },
                selected = selected,
                onClick = { onSelectTab(tab) }
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ShellBottomBar(
    navigator: Navigator,
    onSelectTab: (FoundationTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        navigator.tabs.forEach { tab ->
            val selected = tab == navigator.currentTab

            NavigationBarItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = stringResource(tab.title)
                    )
                },
                label = { Text(stringResource(tab.title)) },
                selected = selected,
                onClick = { onSelectTab(tab) }
            )
        }
    }
}

@Composable
internal fun isPortraitWindow(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return size.width <= size.height
}
