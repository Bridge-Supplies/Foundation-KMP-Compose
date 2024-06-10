import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

class Foundation {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = application {
            KoinInitializer().init()
            
            Window(
                title = stringResource(Res.string.app_name),
                onCloseRequest = ::exitApplication,
                state = rememberWindowState()
            ) {
                App() { isPortraitMode ->
                    // Unnecessary for Desktop
                }
            }
        }
    }
}