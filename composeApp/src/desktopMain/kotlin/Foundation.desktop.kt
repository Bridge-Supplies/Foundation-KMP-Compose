import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import screens.App
import java.awt.Dimension

class Foundation {
    companion object {
        val minWidth = 480
        val minHeight = 480
        val startWidth = 800
        val startHeight = 480
        
        @JvmStatic
        fun main(args: Array<String>) = application {
            config.KoinInitializer().init()
            val windowState = rememberWindowState()
            windowState.apply {
                this.size = DpSize(
                    startWidth.dp,
                    startHeight.dp
                )
            }
            
            Window(
                title = stringResource(Res.string.app_name),
                onCloseRequest = ::exitApplication,
                state = windowState
            ) {
                window.apply {
                    minimumSize = Dimension(minWidth, minHeight)
                }
                
                App(
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