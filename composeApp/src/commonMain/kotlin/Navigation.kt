import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class Screens(
    val title: String,
    val route: String,
    val icon: ImageVector
) {
    HOME(
        "Home",
        "home",
        Icons.Outlined.Home
    ),
    SCANNER(
        "Scanner",
        "scanner",
        Icons.Outlined.QrCodeScanner
    ),
    SETTINGS(
        "Settings",
        "settings",
        Icons.Outlined.Settings
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    isPortraitMode: Boolean
) {
    var selectedItem by remember { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf(Screens.HOME.route) }
    
    Screens.entries.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }
    
    NavigationBar {
        Screens.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    currentRoute = item.route
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
        startDestination = Screens.HOME.route
    ) {
        composable(Screens.HOME.route) {
            HomeScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
        
        composable(Screens.SCANNER.route) {
            ScannerScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
        
        composable(Screens.SETTINGS.route) {
            SettingsScreen(
                viewModel = viewModel,
                haptics = haptics,
                isPortraitMode = isPortraitMode()
            )
        }
    }
}