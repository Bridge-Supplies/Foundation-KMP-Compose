package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.Landing
import config.FoundationTheme
import data.MainViewModel
import ui.landing.LandingScreen
import ui.sheets.ActiveBottomSheet
import ui.sheets.DatePickerBottomSheet
import ui.sheets.ShareAppBottomSheet

@Composable
fun App(
    viewModel: MainViewModel,
    onShowSystemUi: (Boolean) -> Unit = { },
    onCloseApplication: () -> Unit = { }
) {
    FoundationTheme(
        viewModel = viewModel
    ) {
        val useFullscreenLandscape by viewModel.useFullscreenLandscape.collectAsState()

        val haptics = LocalHapticFeedback.current
        val hapticFeedback: () -> Unit = {
            viewModel.hapticFeedback(haptics)
        }

        FoundationApp(
            tabs = appTabs,
            routes = appRoutes,
            landing = Landing.Custom { onDone ->
                LandingScreen(
                    viewModel = viewModel,
                    onLandingCompleted = { onDone() }
                )
            },
            fullscreenLandscape = useFullscreenLandscape,
            onShowSystemUi = onShowSystemUi,
            onCloseApplication = onCloseApplication,
            onHapticFeedback = hapticFeedback,
            overlay = {
                BottomSheets(
                    viewModel = viewModel,
                    hapticFeedback = hapticFeedback
                )
            }
        ) {
            appEntries(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback
            )
        }
    }
}

@Composable
private fun BottomSheets(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val currentBottomSheet by viewModel.currentBottomSheet.collectAsState()

    when (val sheet = currentBottomSheet) {
        is ActiveBottomSheet.None -> {
            /* no-op */
        }

        is ActiveBottomSheet.DatePicker -> {
            DatePickerBottomSheet(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback,
                initialSelectedDateMs = sheet.selectedDate,
                onDateSelected = sheet.onDateSelected
            )
        }

        is ActiveBottomSheet.ShareApp -> {
            ShareAppBottomSheet(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback
            )
        }
    }
}
