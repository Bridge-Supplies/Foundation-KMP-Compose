import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App(
    onShowSystemUi: (Boolean) -> Unit = { }
) {
    KoinContext {
        val viewModel = koinViewModel<MainViewModel>()
        val navController = rememberNavController()
        val haptics = LocalHapticFeedback.current
        
        FoundationTheme(
            viewModel = viewModel
        ) {
            MainScaffold(
                viewModel = viewModel,
                navController = navController,
                haptics = haptics,
                onShowSystemUi = onShowSystemUi
            )
        }
    }
}

@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    navController: NavHostController,
    haptics: HapticFeedback,
    onShowSystemUi: (Boolean) -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val platform = remember { getPlatform() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.entries.find { it.route == backStackEntry?.destination?.route } ?: Screen.HOME
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        topBar = {
            TopBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    navController.navigateUp()
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isPortraitMode,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                BottomNavigationBar(
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        LaunchedEffect(isPortraitMode) {
            onShowSystemUi(isPortraitMode)
        }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    // iOS landscape gives too much horizontal space
                    start = if (!isPortraitMode && platform.type != PlatformType.IOS)
                        innerPadding.calculateStartPadding(LocalLayoutDirection.current) else 0.dp,
                    end = if (!isPortraitMode && platform.type != PlatformType.IOS)
                        innerPadding.calculateEndPadding(LocalLayoutDirection.current) else 0.dp,
                ),
        ) {
            AnimatedVisibility(
                visible = !isPortraitMode,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                SideNavigationRail(
                    navController = navController
                )
            }
            
            Navigation(
                modifier = Modifier
                    .fillMaxSize(),
                viewModel = viewModel,
                navController = navController,
                haptics = haptics
            )
        }
    }
}