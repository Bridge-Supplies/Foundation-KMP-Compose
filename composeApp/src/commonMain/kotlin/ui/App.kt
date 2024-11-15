package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import config.FoundationTheme
import config.PlatformType
import config.getPlatform
import config.isPortraitMode
import data.MainViewModel
import data.koinViewModel
import org.koin.compose.KoinContext
import ui.sheets.ActiveBottomSheet
import ui.sheets.DatePickerBottomSheet
import ui.sheets.ShareAppBottomSheet

@Composable
fun App(
    onShowSystemUi: (Boolean) -> Unit = { },
    onCloseApplication: () -> Unit = { }
) {
    KoinContext {
        val viewModel = koinViewModel<MainViewModel>()
        
        FoundationTheme(
            viewModel = viewModel
        ) {
            MainScaffold(
                viewModel = viewModel,
                onShowSystemUi = onShowSystemUi,
                onCloseApplication = onCloseApplication
            )
        }
    }
}

@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    onShowSystemUi: (Boolean) -> Unit,
    onCloseApplication: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    
    val platform = remember { getPlatform() }
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val isNavigatingTopLevel = remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    
    val useFullscreenLandscape by viewModel.useFullscreenLandscape.collectAsState()
    val currentBottomSheet by viewModel.currentBottomSheet.collectAsState()
    
    val haptics = LocalHapticFeedback.current
    val onVibrate: () -> Unit = {
        viewModel.hapticFeedback(haptics)
    }
    
    val onSelectNavigationTab = { tab: NavigationTab ->
        isNavigatingTopLevel.value = true
        selectNavigationTab(navController, tab, onVibrate)
    }
    
    val onNavigateBack: () -> Unit = {
        onVibrate()
        isNavigatingTopLevel.value = false
        navController.navigateUp()
    }
    
    // track navigation events to update UI
    var currentScreen: Screen? by remember { mutableStateOf(Screen.LANDING) }
    var currentlySelectedTab: NavigationTab? by remember { mutableStateOf(null) }
    var canNavigateBack by remember { mutableStateOf(false) }
    
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            currentScreen = controller.currentBackStackEntry?.getCurrentScreen()
            currentlySelectedTab = currentScreen?.getNavigationTab()
            val isAtStartDestination = currentlySelectedTab?.startDestination?.route == controller.currentDestination?.route
            canNavigateBack = !isAtStartDestination && controller.previousBackStackEntry != null
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .displayCutoutPadding()
    ) {
        AnimatedVisibility(
            visible = currentlySelectedTab != null && !isPortraitMode,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            SideNavigationRail(
                currentlySelectedTab = currentlySelectedTab,
                onSelectNavigationTab = onSelectNavigationTab
            )
        }
        
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                AnimatedVisibility(
                    visible = currentlySelectedTab != null,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TopBar(
                        viewModel = viewModel,
                        currentlySelectedTab = currentlySelectedTab,
                        currentScreen = currentScreen,
                        canNavigateBack = canNavigateBack,
                        onNavigateBack = onNavigateBack
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = currentlySelectedTab != null && isPortraitMode,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BottomAppBar(
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        BottomNavigationBar(
                            currentlySelectedTab = currentlySelectedTab,
                            onSelectNavigationTab = onSelectNavigationTab
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHost)
            }
        ) { innerPadding ->
            LaunchedEffect(isPortraitMode, useFullscreenLandscape) {
                if (isPortraitMode || !useFullscreenLandscape) {
                    onShowSystemUi(true)
                } else {
                    onShowSystemUi(false)
                }
            }
            
            NavigationGraph(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        // iOS landscape gives too much horizontal space and we can't tell which side is too wide
                        start = if (!isPortraitMode && platform.type != PlatformType.IOS)
                            innerPadding.calculateStartPadding(LocalLayoutDirection.current) else 0.dp,
                        end = if (!isPortraitMode && platform.type != PlatformType.IOS)
                            innerPadding.calculateEndPadding(LocalLayoutDirection.current) else 0.dp,
                    ),
                viewModel = viewModel,
                navController = navController,
                isNavigatingTopLevel = isNavigatingTopLevel,
                snackbarHost = snackbarHost,
                onVibrate = onVibrate,
                onNavigateBack = onNavigateBack,
                onCloseApplication = onCloseApplication
            )
            
            when (currentBottomSheet) {
                is ActiveBottomSheet.None -> {
                    /* no-op */
                }
                
                is ActiveBottomSheet.DatePicker -> {
                    val sheetData = (currentBottomSheet as ActiveBottomSheet.DatePicker)
                    DatePickerBottomSheet(
                        viewModel = viewModel,
                        coroutineScope = coroutineScope,
                        selectedDate = sheetData.selectedDate,
                        onVibrate = onVibrate
                    )
                }
                
                is ActiveBottomSheet.ShareApp -> {
                    val sheetData = (currentBottomSheet as ActiveBottomSheet.ShareApp)
                    ShareAppBottomSheet(
                        viewModel = viewModel,
                        coroutineScope = coroutineScope,
                        onVibrate = onVibrate
                    )
                }
            }
        }
    }
}