import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.navigation_back
import org.jetbrains.compose.resources.stringResource

enum class Screen(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val filled: ImageVector
) {
    HOME(
        "Home",
        "home",
        Icons.Outlined.Home,
        Icons.Filled.Home
    ),
    SCANNER(
        "Scanner",
        "scanner",
        Icons.Outlined.QrCodeScanner,
        Icons.Filled.QrCodeScanner
    ),
    SETTINGS(
        "Settings",
        "settings",
        Icons.Outlined.Settings,
        Icons.Filled.Settings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {}
) {
    val platform = remember { getPlatform() }
    
    if (platform.type == PlatformType.ANDROID) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, navigateUp)
            }
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                TopBarNavIcon(canNavigateBack, navigateUp)
            }
        )
    }
}

@Composable
fun TopBarNavIcon(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit
) {
    AnimatedVisibility(
        visible = canNavigateBack,
        modifier = Modifier
            .fillMaxHeight()
    ) {
        IconButton(
            onClick = navigateUp
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
    navController: NavController
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
    
    ) {
        Screen.entries.forEachIndexed { index, item ->
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.HOME.route
            val selected = currentRoute == item.route
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
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    NavigationBar {
        Screen.entries.forEachIndexed { index, item ->
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.HOME.route
            val selected = currentRoute == item.route
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
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController,
    haptics: HapticFeedback
) {
    // Note: iOS does not propagate composition changes through NavHost
    // Params must be declared inside each composable()
    
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.HOME.route
    ) {
        composable(Screen.HOME.route) {
            HomeScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
        
        composable(Screen.SCANNER.route) {
            ScannerScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
        
        composable(Screen.SETTINGS.route) {
            SettingsScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
    }
}