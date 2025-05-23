import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.MainViewModel
import data.koinViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import ui.App
import java.awt.Dimension

class Foundation {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = application {
            config.KoinInitializer().init()
            
            val windowState = rememberWindowState()
            
            Window(
                title = stringResource(Res.string.app_name),
                onCloseRequest = ::exitApplication,
                state = windowState
            ) {
                KoinContext {
                    val viewModel = koinViewModel<MainViewModel>()
                    
                    window.minimumSize = Dimension(480, 480)
                    
                    LaunchedEffect(Unit) {
                        window.size = Dimension(
                            viewModel.desktopResolutionX.value,
                            viewModel.desktopResolutionY.value
                        )
                    }
                    
                    LaunchedEffect(windowState.size.width) {
                        viewModel.setDesktopResolutionX(windowState.size.width.value.toInt())
                    }
                    
                    LaunchedEffect(windowState.size.height) {
                        viewModel.setDesktopResolutionY(windowState.size.height.value.toInt())
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
}