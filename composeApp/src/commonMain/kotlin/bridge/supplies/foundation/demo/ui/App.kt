package bridge.supplies.foundation.demo.ui

import androidx.compose.runtime.Composable
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.Landing
import bridge.supplies.foundation.ui.theme.ExpressiveAnimation
import bridge.supplies.foundation.ui.theme.ThemeConfig
import bridge.supplies.foundation.demo.data.MainViewModel
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
        // The demo lets users switch motion schemes; apps usually keep the default (always expressive)
        theme = ThemeConfig(expressiveAnimation = ExpressiveAnimation.USER_CHOICE),
        onShowSystemUi = onShowSystemUi,
        onCloseApplication = onCloseApplication
    ) {
        appEntries(
            viewModel = viewModel,
            hapticFeedback = hapticFeedback
        )
    }
}
