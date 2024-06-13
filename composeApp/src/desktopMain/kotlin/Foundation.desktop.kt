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
        @JvmStatic
        fun main(args: Array<String>) = application {
            config.KoinInitializer().init()
            
            Window(
                title = stringResource(Res.string.app_name),
                onCloseRequest = ::exitApplication,
                state = rememberWindowState()
            ) {
                window.apply {
                    minimumSize = Dimension(450, 450)
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