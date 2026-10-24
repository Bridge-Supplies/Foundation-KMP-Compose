package ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Settings
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.ui.shell.FoundationTab
import bridge.supplies.foundation.ui.shell.LocalSnackbarHostState
import bridge.supplies.foundation.ui.shell.LocalNavigator
import bridge.supplies.foundation.ui.shell.RouteRegistry
import bridge.supplies.foundation.ui.shell.screenMetadata
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.screen_about_title
import foundation.composeapp.generated.resources.screen_home_columns_title
import foundation.composeapp.generated.resources.screen_home_date_title
import foundation.composeapp.generated.resources.screen_home_grids_title
import foundation.composeapp.generated.resources.screen_home_rows_title
import foundation.composeapp.generated.resources.screen_scan_code_title
import foundation.composeapp.generated.resources.tab_home_title
import foundation.composeapp.generated.resources.tab_settings_title
import foundation.composeapp.generated.resources.tab_share_title
import kotlinx.serialization.Serializable
import ui.home.HomeColumnsScreen
import ui.home.HomeDateScreen
import ui.home.HomeGridsScreen
import ui.home.HomeInfoScreen
import ui.home.HomeRowsScreen
import ui.scanner.ScanCodeScreen
import ui.scanner.ShareCodeScreen
import ui.settings.SettingsAboutScreen
import ui.settings.SettingsOptionsScreen

// ROUTES

@Serializable data object HomeInfo : NavKey
@Serializable data class HomeDate(val selectedDateMs: Long) : NavKey
@Serializable data object HomeColumns : NavKey
@Serializable data object HomeRows : NavKey
@Serializable data object HomeGrids : NavKey

@Serializable data object ShareGenerate : NavKey
@Serializable data object ShareScan : NavKey

@Serializable data object SettingsOptions : NavKey
@Serializable data object SettingsAbout : NavKey

val appRoutes = RouteRegistry {
    subclass(HomeInfo::class, HomeInfo.serializer())
    subclass(HomeDate::class, HomeDate.serializer())
    subclass(HomeColumns::class, HomeColumns.serializer())
    subclass(HomeRows::class, HomeRows.serializer())
    subclass(HomeGrids::class, HomeGrids.serializer())
    subclass(ShareGenerate::class, ShareGenerate.serializer())
    subclass(ShareScan::class, ShareScan.serializer())
    subclass(SettingsOptions::class, SettingsOptions.serializer())
    subclass(SettingsAbout::class, SettingsAbout.serializer())
}


// TABS

val appTabs = listOf(
    FoundationTab(
        id = "home",
        title = Res.string.tab_home_title,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        root = HomeInfo
    ),
    FoundationTab(
        id = "share",
        title = Res.string.tab_share_title,
        icon = Icons.Outlined.QrCode,
        selectedIcon = Icons.Filled.QrCode,
        root = ShareGenerate
    ),
    FoundationTab(
        id = "settings",
        title = Res.string.tab_settings_title,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        root = SettingsOptions
    )
)


// SCREENS

fun EntryProviderScope<NavKey>.appEntries(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    // HOME
    entry<HomeInfo> {
        val navigator = LocalNavigator.current
        HomeInfoScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback,
            onNavigateDateScreen = { selectedDate -> navigator.navigate(HomeDate(selectedDate)) },
            onNavigateColumnsScreen = { navigator.navigate(HomeColumns) },
            onNavigateRowsScreen = { navigator.navigate(HomeRows) },
            onNavigateGridsScreen = { navigator.navigate(HomeGrids) }
        )
    }

    entry<HomeDate>(metadata = screenMetadata(title = Res.string.screen_home_date_title)) { route ->
        HomeDateScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback,
            initialSelectedDateMs = route.selectedDateMs
        )
    }

    entry<HomeColumns>(metadata = screenMetadata(title = Res.string.screen_home_columns_title)) {
        HomeColumnsScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }

    entry<HomeRows>(metadata = screenMetadata(title = Res.string.screen_home_rows_title)) {
        HomeRowsScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }

    entry<HomeGrids>(metadata = screenMetadata(title = Res.string.screen_home_grids_title)) {
        HomeGridsScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }

    // SHARE
    entry<ShareGenerate> {
        val navigator = LocalNavigator.current
        ShareCodeScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback,
            onNavigateToScanner = { navigator.navigate(ShareScan) }
        )
    }

    entry<ShareScan>(metadata = screenMetadata(title = Res.string.screen_scan_code_title)) {
        val navigator = LocalNavigator.current
        ScanCodeScreen(
            viewModel = viewModel,
            snackbarHost = LocalSnackbarHostState.current,
            hapticFeedback = hapticFeedback,
            onSuccessfulScan = { sharedText ->
                viewModel.setSharedText(sharedText)
                navigator.back()
            },
            onPermissionDenied = {
                hapticFeedback()
                navigator.back()
            }
        )
    }

    // SETTINGS
    entry<SettingsOptions> {
        val navigator = LocalNavigator.current
        SettingsOptionsScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback,
            onNavigateToAbout = { navigator.navigate(SettingsAbout) }
        )
    }

    entry<SettingsAbout>(metadata = screenMetadata(title = Res.string.screen_about_title)) {
        SettingsAboutScreen(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }
}
