package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import config.ColorSchemeStyle
import config.PlatformType
import config.getAppliedColorScheme
import config.getPlatform
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.navigation_back
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
    val title: String,
    val route: String,
    val icon: ImageVector,
    val filled: ImageVector,
    val startDestination: Screen,
    val screens: List<Screen>
) {
    HOME(
        "Home",
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
        "Share",
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
        "Settings",
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

enum class Screen(
    val title: String,
    val route: String
) {
    // LANDING
    LANDING(
        "Landing",
        "landing"
    ),
    
    // HOME
    HOME_INFO(
        "Info",
        "home_info"
    ),
    HOME_DATE(
        "Date",
        "home_date"
    ),
    
    // SHARE
    SHARE_GENERATE(
        "Generate Code",
        "share_generate"
    ),
    SHARE_SCAN(
        "Scan Code",
        "share_scan"
    ),
    
    // SETTINGS
    SETTINGS_OPTIONS(
        "Options",
        "settings_options"
    ),
    SETTINGS_ABOUT(
        "About",
        "settings_about"
    )
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
    onVibrate: () -> Unit,
    onNavigateBack: () -> Unit,
    onCloseApplication: () -> Unit
) {
    // Note: iOS does not propagate composition changes through NavHost
    // Params must be declared inside each composable(), ie isPortraitMode()
    
    val enterTransition = if (isNavigatingTopLevel.value) TopLevelEnterTransition() else ScreenEnterTransition()
    val exitTransition = if (isNavigatingTopLevel.value) TopLevelExitTransition() else ScreenExitTransition()
    val popEnterTransition = if (isNavigatingTopLevel.value) TopLevelEnterTransition() else ScreenPopEnterTransition()
    val popExitTransition = if (isNavigatingTopLevel.value) TopLevelExitTransition() else ScreenPopExitTransition()
    
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "landing_navigation",
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        navigation(
            route = "landing_navigation",
            startDestination = Screen.LANDING.route,
        ) {
            composable(
                route = Screen.LANDING.route
            ) {
                BackHandler {
                    onCloseApplication()
                }
                
                LandingScreen() {
                    isNavigatingTopLevel.value = true
                    navController.popBackStack()
                    selectNavigationTab(navController, NavigationTab.HOME)
                }
            }
        }
        
        navigation(
            route = NavigationTab.HOME.route,
            startDestination = NavigationTab.HOME.startDestination.route
        ) {
            composable(
                route = Screen.HOME_INFO.route
            ) {
                BackHandler {
                    onCloseApplication()
                }
                
                HomeInfoScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate,
                    onNavigateDateScreen = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.HOME_DATE)
                    }
                )
            }
            
            composable(
                route = Screen.HOME_DATE.route
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                HomeDateScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate
                )
            }
        }
        
        navigation(
            route = NavigationTab.SHARE.route,
            startDestination = NavigationTab.SHARE.startDestination.route
        ) {
            composable(
                route = Screen.SHARE_GENERATE.route
            ) {
                BackHandler {
                    isNavigatingTopLevel.value = true
                    selectNavigationTab(navController, NavigationTab.HOME, onVibrate)
                }
                
                ShareCodeScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate,
                    onNavigateToScanner = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.SHARE_SCAN)
                    }
                )
            }
            
            composable(
                route = Screen.SHARE_SCAN.route
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                ScanCodeScreen(
                    viewModel = viewModel,
                    snackbarHost = snackbarHost,
                    onVibrate = onVibrate,
                    onCloseScanner = {
                        isNavigatingTopLevel.value = false
                        navController.navigateUp()
                    }
                )
            }
        }
        
        navigation(
            route = NavigationTab.SETTINGS.route,
            startDestination = NavigationTab.SETTINGS.startDestination.route
        ) {
            composable(
                route = Screen.SETTINGS_OPTIONS.route
            ) {
                BackHandler {
                    isNavigatingTopLevel.value = true
                    selectNavigationTab(navController, NavigationTab.HOME, onVibrate)
                }
                
                SettingsOptionsScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate,
                    onNavigateToAbout = {
                        isNavigatingTopLevel.value = false
                        navigateToScreen(navController, Screen.SETTINGS_ABOUT)
                    }
                )
            }
            
            composable(
                route = Screen.SETTINGS_ABOUT.route
            ) {
                BackHandler {
                    onNavigateBack()
                }
                
                SettingsAboutScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentlySelectedTab: NavigationTab?,
    currentScreen: Screen?,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit = {}
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val platform = remember { getPlatform() }
    val appName = stringResource(Res.string.app_name)
    val title = remember(currentScreen) {
        when {
            currentlySelectedTab != null && currentScreen != null && currentlySelectedTab.startDestination != currentScreen ->
                currentScreen.title
            
            currentlySelectedTab != null ->
                currentlySelectedTab.title
            
            else -> appName
        }
    }
    
    if (platform.type == PlatformType.ANDROID) {
        TopAppBar(
            title = {
                TopBarText(
                    text = title,
                    textColor = colorScheme.onContentColor
                )
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, onNavigateBack)
            }
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                TopBarText(
                    text = title,
                    textColor = colorScheme.onContentColor
                )
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, onNavigateBack)
            }
        )
    }
}

@Composable
fun TopBarText(
    text: String,
    textColor: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = textColor,
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
        IconButton(
            onClick = onNavigateBack,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.navigation_back)
            )
        }
    }
}

@Composable
fun SideNavigationRail(
    currentlySelectedTab: NavigationTab?,
    onSelectNavigationTab: (NavigationTab) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        NavigationTab.entries.forEach { item ->
            val selected = item.route == currentlySelectedTab?.route
            
            NavigationRailItem(
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) item.filled else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    onSelectNavigationTab(item)
                }
            )
        }
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
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
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
    Screen.entries.find { it.route == destination.route } ?: Screen.LANDING

fun NavBackStackEntry.getSelectedNavigationTab(): NavigationTab? {
    val currentScreen = getCurrentScreen()
    val selectedTab = currentScreen.getNavigationTab()
    return selectedTab
}

fun selectNavigationTab(
    navController: NavHostController,
    navigationTab: NavigationTab,
    onVibrate: () -> Unit = {}
) {
    val currentScreen = navController.currentBackStackEntry?.getCurrentScreen()
    val currentlySelectedTab = currentScreen?.getNavigationTab()
    
    if (navigationTab.startDestination != currentScreen) {
        onVibrate()
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
    navController: NavController,
    screen: Screen
) {
    // TODO: set up + test cross-screen navigation
//    val targetNavigationTab = screen.getNavigationTab()
//    val currentNavigationTab = navController.currentBackStackEntry?.getSelectedNavigationTab()
//
//    if (targetNavigationTab != null && targetNavigationTab != currentNavigationTab) {
//        selectNavigationTab(navController, targetNavigationTab)
//    }
    
    navController.navigate(screen.route)
}