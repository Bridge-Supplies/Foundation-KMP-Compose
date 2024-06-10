import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

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
fun SideNavigationRail(
    navController: NavController
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Screens.entries.forEachIndexed { index, item ->
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screens.HOME.route
            NavigationRailItem(
                alwaysShowLabel = true,
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    NavigationBar {
        Screens.entries.forEachIndexed { index, item ->
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screens.HOME.route
            NavigationBarItem(
                alwaysShowLabel = true,
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) }
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