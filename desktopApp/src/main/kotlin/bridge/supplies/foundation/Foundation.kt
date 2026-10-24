package bridge.supplies.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.util.koinViewModel
import bridge.supplies.foundation.demo.data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import bridge.supplies.foundation.demo.ui.App
import java.awt.Dimension

class Foundation {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            bridge.supplies.foundation.demo.config.KoinInitializer().init()

            // Read the stored window size before showing the window, so the default size can't overwrite it
            val settings = GlobalContext.get().get<FoundationSettings>()
            val (storedWidth, storedHeight) = runBlocking {
                settings.get(FoundationSettingKeys.desktopResolutionX) to settings.get(FoundationSettingKeys.desktopResolutionY)
            }

            application {
                FoundationWindow(
                    settings = settings,
                    initialSize = DpSize(storedWidth.dp, storedHeight.dp),
                    onExit = ::exitApplication
                )
            }
        }

        @Composable
        private fun FoundationWindow(
            settings: FoundationSettings,
            initialSize: DpSize,
            onExit: () -> Unit
        ) {
            val windowState = rememberWindowState(size = initialSize)

            Window(
                title = stringResource(Res.string.app_name),
                onCloseRequest = onExit,
                state = windowState
            ) {
                val viewModel = koinViewModel<MainViewModel>()

                window.minimumSize = Dimension(480, 480)

                LaunchedEffect(windowState.size) {
                    settings.set(FoundationSettingKeys.desktopResolutionX, windowState.size.width.value.toInt())
                    settings.set(FoundationSettingKeys.desktopResolutionY, windowState.size.height.value.toInt())
                }

                App(
                    viewModel = viewModel,
                    onShowSystemUi = { isPortraitMode ->
                        // unnecessary for desktop
                    },
                    onCloseApplication = {
                        // unnecessary for desktop
                    }
                )
            }
        }
    }
}
