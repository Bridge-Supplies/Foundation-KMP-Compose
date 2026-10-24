package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.Landing
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.ic_launcher_foreground
import ui.sheets.ActiveBottomSheet
import ui.sheets.DatePickerBottomSheet
import ui.sheets.ShareAppBottomSheet

@Composable
fun App(
    viewModel: MainViewModel,
    onShowSystemUi: (Boolean) -> Unit = { },
    onCloseApplication: () -> Unit = { }
) {
    val hapticFeedback = rememberHapticFeedback()

    FoundationApp(
        tabs = appTabs,
        routes = appRoutes,
        landing = Landing.Default(icon = Res.drawable.ic_launcher_foreground),
        onShowSystemUi = onShowSystemUi,
        onCloseApplication = onCloseApplication,
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
