import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    navController: NavHostController,
    haptics: HapticFeedback,
    onShowSystemUi: (Boolean) -> Unit
) {
    val isPortraitMode = isPortraitMode()
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = if (isPortraitMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isPortraitMode,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BottomNavigationBar(
                        navController = navController
                    )
                }
            }
        }
    ) { innerPadding ->
        LaunchedEffect(isPortraitMode) {
            onShowSystemUi(!isPortraitMode)
        }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
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