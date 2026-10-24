package ui

import androidx.compose.runtime.Composable
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.Landing
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.ic_launcher_foreground

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
        onCloseApplication = onCloseApplication
    ) {
        appEntries(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }
}
