package screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import config.PlatformType
import config.getPlatform
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.navigation_back
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import screens.home.HomeDateScreen
import screens.home.HomeInfoScreen
import screens.landing.LandingScreen
import screens.scanner.ScanCodeScreen
import screens.scanner.ShareCodeScreen
import screens.settings.SettingsAboutScreen
import screens.settings.SettingsOptionsScreen

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

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    onVibrate: () -> Unit,
    onCloseApplication: () -> Unit
) {
    // Note: iOS does not propagate composition changes through NavHost
    // Params must be declared inside each composable(), ie isPortraitMode()
    
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "landing_navigation"
    ) {
        navigation(
            route = "landing_navigation",
            startDestination = Screen.LANDING.route
        ) {
            composable(
                route = Screen.LANDING.route
            ) {
                BackHandler {
                    onCloseApplication()
                }
                
                LaunchedEffect(Unit) {
                    delay(1000)
                    navController.popBackStack()
                    selectNavigationTab(navController, NavigationTab.HOME)
                }
                
                LandingScreen()
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
                    onNavTest = {
                        navigateToScreen(navController, Screen.HOME_DATE)
                    }
                )
            }
            
            composable(
                route = Screen.HOME_DATE.route
            ) {
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
                    selectNavigationTab(navController, NavigationTab.HOME)
                }
                
                ShareCodeScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate,
                    onNavigateToScanner = {
                        navigateToScreen(navController, Screen.SHARE_SCAN)
                    }
                )
            }
            
            composable(
                route = Screen.SHARE_SCAN.route
            ) {
                ScanCodeScreen(
                    viewModel = viewModel,
                    snackbarHost = snackbarHost,
                    onVibrate = onVibrate,
                    onCloseScanner = {
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
                    selectNavigationTab(navController, NavigationTab.HOME)
                }
                
                SettingsOptionsScreen(
                    viewModel = viewModel,
                    onVibrate = onVibrate,
                    onNavigateToAbout = {
                        navigateToScreen(navController, Screen.SETTINGS_ABOUT)
                    }
                )
            }
            
            composable(
                route = Screen.SETTINGS_ABOUT.route
            ) {
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
    val platform = remember { getPlatform() }
    val appName = stringResource(Res.string.app_name)
    val title = remember(currentScreen) {
        when {
            currentlySelectedTab != null && currentScreen != null && currentlySelectedTab.startDestination != currentScreen ->
                "$appName > ${currentlySelectedTab.title} > ${currentScreen.title}"
            
            currentlySelectedTab != null ->
                "$appName > ${currentlySelectedTab.title}"
            
            else -> appName
        }
    }
    
    if (platform.type == PlatformType.ANDROID) {
        TopAppBar(
            title = {
                TopBarText(title)
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, onNavigateBack)
            }
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                TopBarText(title)
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, onNavigateBack)
            }
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
        color = MaterialTheme.colorScheme.onSurface,
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
    navController: NavController,
    navigationTab: NavigationTab
) {
    val currentlySelectedTab = navController.currentBackStackEntry?.getSelectedNavigationTab()
    
    navController.navigate(navigationTab.route) {
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
        
        launchSingleTop = true
        restoreState = true
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