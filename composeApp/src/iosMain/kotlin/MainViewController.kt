import androidx.compose.ui.window.ComposeUIViewController
import bridge.supplies.foundation.demo.data.MainViewModel
import bridge.supplies.foundation.demo.ui.App
import bridge.supplies.foundation.ui.util.koinViewModel

fun MainViewController() = ComposeUIViewController(
    configure = {
        bridge.supplies.foundation.demo.config.KoinInitializer().init()
    }
) {
    val viewModel = koinViewModel<MainViewModel>()
    
    App(
        viewModel = viewModel,
        onShowSystemUi = { isPortraitMode ->
            // iOS automatically hides UI in portrait mode
        },
        onCloseApplication = {
            // unnecessary for iOS
        }
    )
}