import androidx.compose.ui.window.ComposeUIViewController
import data.MainViewModel
import data.koinViewModel
import ui.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        config.KoinInitializer().init()
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