package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.savedstate.read
import config.PlatformType
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.action_settings
import foundation.composeapp.generated.resources.action_share
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.navigation_back
import foundation.composeapp.generated.resources.screen_about_title
import foundation.composeapp.generated.resources.screen_generate_code_title
import foundation.composeapp.generated.resources.screen_home_date_title
import foundation.composeapp.generated.resources.screen_home_info_title
import foundation.composeapp.generated.resources.screen_landing_title
import foundation.composeapp.generated.resources.screen_options_title
import foundation.composeapp.generated.resources.screen_scan_code_title
import foundation.composeapp.generated.resources.tab_home_title
import foundation.composeapp.generated.resources.tab_settings_title
import foundation.composeapp.generated.resources.tab_share_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ui.home.HomeDateScreen
import ui.home.HomeInfoScreen
import ui.landing.LandingScreen
import ui.scanner.ScanCodeScreen
import ui.scanner.ShareCodeScreen
import ui.settings.SettingsAboutScreen
import ui.settings.SettingsOptionsScreen

// SCREENS

enum class NavigationTab(
    val titleRes: StringResource,
    val route: String,
    val icon: ImageVector,
    val filled: ImageVector,
    val startDestination: Screen,
    val screens: List<Screen>
) {
    HOME(
        Res.string.tab_home_title,
        "home",
        Icons.Outlined.Home,
        Icons.Filled.Home,
        Screen.HOME_INFO,
        listOf(
            Screen.HOME_INFO,
            Screen.HOME_DATE
        )
    ),
    
    SHARE(
        Res.string.tab_share_title,
        "share",
        Icons.Outlined.QrCode,
        Icons.Filled.QrCode,
        Screen.SHARE_GENERATE,
        listOf(
            Screen.SHARE_GENERATE,
            Screen.SHARE_SCAN
        )
    ),
    
    SETTINGS(
        Res.string.tab_settings_title,
        "settings",
        Icons.Outlined.Settings,
        Icons.Filled.Settings,
        Screen.SETTINGS_OPTIONS,
        listOf(
            Screen.SETTINGS_OPTIONS,
            Screen.SETTINGS_ABOUT
        )
    )
}

enum class AppBarAction(
    val labelRes: StringResource,
    val icon: ImageVector
) {
    SHARE(
        Res.string.action_share,
        Icons.Default.Share
    ),
    SETTINGS(
        Res.string.action_settings,
        Icons.Default.Settings
    )
}

enum class Screen(
    val titleRes: StringResource,
    val route: String,
    val arg: NavArgument? = null,
    val actions: List<AppBarAction>
) {
    // LANDING
    LANDING(
        Res.string.screen_landing_title,
        "landing",
        null,
        emptyList()
    ),
    
    // HOME
    HOME_INFO(
        Res.string.screen_home_info_title,
        "home_info",
        null,
        emptyList()
    ),
    HOME_DATE(
        Res.string.screen_home_date_title,
        "home_date",
        null,
        emptyList()
    ),
    
    // SHARE
    SHARE_GENERATE(
        Res.string.screen_generate_code_title,
        "share_generate",
        null,
        listOf(AppBarAction.SHARE)
    ),
    SHARE_SCAN(
        Res.string.screen_scan_code_title,
        "share_scan",
        null,
        emptyList()
    ),
    
    // SETTINGS
    SETTINGS_OPTIONS(
        Res.string.screen_options_title,
        "settings_options",
        null,
        listOf(AppBarAction.SETTINGS)
    ),
    SETTINGS_ABOUT(
        Res.string.screen_about_title,
        "settings_about",
        null,
        emptyList()
    );
    
    fun getNavRoute(): String =
        route + (arg?.key?.let { "?$it={$it}" } ?: "")
}

enum class NavArgument(
    val key: String,
    val type: NavType<*>,
    val defaultValue: Any
) {
    // TODO
}

fun arg(argument: NavArgument): NamedNavArgument =
    navArgument(argument.key) { type = argument.type; defaultValue = argument.defaultValue }

fun NavBackStackEntry.getString(argument: NavArgument): String? {
    return arguments?.read { getString(argument.key) }
}

fun NavBackStackEntry.getInt(argument: NavArgument): Int? {
    return arguments?.read { getInt(argument.key) }
}


// COMPOSABLES

@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit = { }
)

expect val TRANSITION_ENTER_MS: Int
expect val TRANSITION_EXIT_MS: Int
expect val TRANSITION_EASING: Easing
expect val TRANSITION_OFFSET_DIV: Int

fun TopLevelEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = TRANSITION_ENTER_MS,
            easing = TRANSITION_EASING
        )
    )

fun TopLevelExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = TRANSITION_EXIT_MS,
            easing = TRANSITION_EASING
        )
    )

expect fun ScreenEnterTransition(): EnterTransition
expect fun ScreenExitTransition(): ExitTransition
expect fun ScreenPopEnterTransition(): EnterTransition
expect fun ScreenPopExitTransition(): ExitTransition

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController,
    isNavigatingTopLevel: MutableState<Boolean>,
    snackbarHost: SnackbarHostState,
    hapticFeedback: () -> Unit,
    onNavigateBack: () -> Unit,
    onCloseApplication: () -> Unit
) {
    // Note: iOS does not propagate composition changes through NavHost
    // Params must be declared inside each composable(), ie isPortraitMode()
    
    val enterTransition = if (isNavigatingTopLevel.value) TopLevelEnterTransition() else ScreenEnterTransition()
    val exitTransition = if (isNavigatingTopLevel.value) TopLevelExitTransition() else ScreenExitTransition()
    val popEnterTransition = if (isNavigatingTopLevel.value) TopLevelEnterTransition() else ScreenPopEnterTransition()
    val popExitTransition = if (isNavigatingTopLevel.value) TopLevelExitTransition() else ScreenPopExitTransition()
    
    val startDestinationRoute = "landing_navigation"
    
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestinationRoute,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        navigation(
            route = startDestinationRoute,
            startDestination = Screen.LANDING.getNavRoute(),
        ) {
            composable(
                route = Screen.LANDING.getNavRoute()
            ) {
                BackHandler {
                    onCloseApplication()
                }
                
                LandingScreen(
                    viewModel = viewModel
                ) {
                    isNavigatingTopLevel.value = true
                    navController.popBackStack()
                    selectNavigationTab(navController, NavigationTab.HOME)
                }
            }
        }
        
        navigation(
            route = NavigationTab.HOME.route,
            startDestination = NavigationTab.HOME.startDestination.getNavRoute()
        ) {
            composable(
                route = Screen.HOME_INFO.getNavRoute()
            ) {
                BackHandler {
                    onCloseApplication()
                }
                
                HomeInfoScreen(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback,
                    onNavigateDateScreen = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.HOME_DATE)
                    }
                )
            }
            
            composable(
                route = Screen.HOME_DATE.getNavRoute()
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                HomeDateScreen(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback
                )
            }
        }
        
        navigation(
            route = NavigationTab.SHARE.route,
            startDestination = NavigationTab.SHARE.startDestination.getNavRoute()
        ) {
            composable(
                route = Screen.SHARE_GENERATE.getNavRoute()
            ) {
                BackHandler {
                    isNavigatingTopLevel.value = true
                    selectNavigationTab(navController, NavigationTab.HOME, hapticFeedback)
                }
                
                ShareCodeScreen(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback,
                    onNavigateToScanner = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.SHARE_SCAN)
                    }
                )
            }
            
            composable(
                route = Screen.SHARE_SCAN.getNavRoute()
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                ScanCodeScreen(
                    viewModel = viewModel,
                    snackbarHost = snackbarHost,
                    hapticFeedback = hapticFeedback,
                    onSuccessfulScan = { sharedText ->
                        isNavigatingTopLevel.value = true
                        viewModel.setSharedText(sharedText)
                        navController.navigateUp()
                    },
                    onPermissionDenied = {
                        onNavigateBack()
                    }
                )
            }
        }
        
        navigation(
            route = NavigationTab.SETTINGS.route,
            startDestination = NavigationTab.SETTINGS.startDestination.getNavRoute()
        ) {
            composable(
                route = Screen.SETTINGS_OPTIONS.getNavRoute()
            ) {
                BackHandler {
                    isNavigatingTopLevel.value = true
                    selectNavigationTab(navController, NavigationTab.HOME, hapticFeedback)
                }
                
                SettingsOptionsScreen(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback,
                    onNavigateToAbout = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.SETTINGS_ABOUT)
                    }
                )
            }
            
            composable(
                route = Screen.SETTINGS_ABOUT.getNavRoute()
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                SettingsAboutScreen(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback
                )
            }
        }
    }
}

@Composable
fun TopAppBarAction(
    isVisible: Boolean,
    appBarAction: AppBarAction,
    onAction: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier
            .wrapContentWidth()
            .fillMaxHeight()
    ) {
        ClickableIcon(
            onClick = onAction,
            imageVector = appBarAction.icon,
            contentDescription = stringResource(appBarAction.labelRes)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    viewModel: MainViewModel,
    currentlySelectedTab: NavigationTab?,
    currentScreen: Screen?,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit = {}
) {
    val title = remember(currentScreen) {
        when {
            currentlySelectedTab != null && currentScreen != null && currentlySelectedTab.startDestination != currentScreen ->
                currentScreen.titleRes
            
            currentlySelectedTab != null ->
                currentlySelectedTab.titleRes
            
            else -> Res.string.app_name
        }
    }
    
    val actions: @Composable() (RowScope.() -> Unit) = {
        AppBarAction.entries.forEach { appBarAction ->
            TopAppBarAction(
                isVisible = currentScreen?.actions?.contains(appBarAction) == true,
                appBarAction = appBarAction
            ) {
                viewModel.startAppBarAction(appBarAction)
            }
        }
    }
    
    if (viewModel.platform.type != PlatformType.IOS) {
        TopAppBar(
            title = { TopBarText(stringResource(title)) },
            navigationIcon = { TopBarNavIcon(canNavigateBack, onNavigateBack) },
            actions = actions
        )
    } else {
        CenterAlignedTopAppBar(
            title = { TopBarText(stringResource(title)) },
            navigationIcon = { TopBarNavIcon(canNavigateBack, onNavigateBack) },
            actions = actions
        )
    }
}

@Composable
fun TopBarText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TopBarNavIcon(
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit
) {
    AnimatedVisibility(
        visible = canNavigateBack,
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

@Composable
fun SideNavigationRail(
    verticallyCentered: Boolean = true,
    currentlySelectedTab: NavigationTab?,
    onSelectNavigationTab: (NavigationTab) -> Unit
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
        
        NavigationTab.entries.forEach { item ->
            val selected = item.route == currentlySelectedTab?.route
            
            NavigationRailItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) item.filled else item.icon,
                        contentDescription = stringResource(item.titleRes)
                    )
                },
                label = { Text(stringResource(item.titleRes)) },
                selected = selected,
                onClick = {
                    onSelectNavigationTab(item)
                }
            )
        }
        
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun BottomNavigationBar(
    currentlySelectedTab: NavigationTab?,
    onSelectNavigationTab: (NavigationTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        NavigationTab.entries.forEach { item ->
            val selected = item.route == currentlySelectedTab?.route
            
            NavigationBarItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) item.filled else item.icon,
                        contentDescription = stringResource(item.titleRes)
                    )
                },
                label = { Text(stringResource(item.titleRes)) },
                selected = selected,
                onClick = {
                    onSelectNavigationTab(item)
                }
            )
        }
    }
}


// UTILITIES

fun Screen.getNavigationTab(): NavigationTab? {
    val selectedTab = NavigationTab.entries.find { navigationTab ->
        navigationTab.screens.contains(this)
    }
    return selectedTab
}

fun NavBackStackEntry.getCurrentScreen(): Screen =
    Screen.entries.find { destination.route?.startsWith(it.route) == true } ?: Screen.LANDING

fun NavBackStackEntry.getSelectedNavigationTab(): NavigationTab? {
    val currentScreen = getCurrentScreen()
    val selectedTab = currentScreen.getNavigationTab()
    return selectedTab
}

fun selectNavigationTab(
    navController: NavHostController,
    navigationTab: NavigationTab,
    hapticFeedback: () -> Unit = { }
) {
    val currentScreen = navController.currentBackStackEntry?.getCurrentScreen()
    val currentlySelectedTab = currentScreen?.getNavigationTab()
    
    if (navigationTab.startDestination != currentScreen) {
        hapticFeedback()
        navController.navigate(
            route = navigationTab.route
        ) {
            launchSingleTop = true
            restoreState = true
            
            if (currentlySelectedTab?.route == navigationTab.route) {
                // if already on the selected tab, clear its backstack
                popUpTo(navigationTab.route) {
                    inclusive = true
                }
            } else {
                // otherwise save & clear current backstack
                popUpTo(currentlySelectedTab?.route ?: return@navigate) {
                    saveState = true
                    inclusive = true
                }
            }
        }
    }
}

fun navigateToScreen(
    navController: NavHostController,
    screen: Screen,
    arg: Any? = null
) {
    val targetNavigationTab = screen.getNavigationTab()
    val currentNavigationTab = navController.currentBackStackEntry?.getSelectedNavigationTab()
    
    if (targetNavigationTab != null && targetNavigationTab != currentNavigationTab) {
        selectNavigationTab(navController, targetNavigationTab)
    }
    
    val routeWithArgs = if (arg != null && screen.arg != null) {
        screen.route + "?${screen.arg.key}" + "=$arg"
    } else {
        screen.route
    }
    
    navController.navigate(routeWithArgs)
}