import androidx.compose.ui.window.ComposeUIViewController
import screens.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        config.KoinInitializer().init()
    }
) {
    App(
        onShowSystemUi = { isPortraitMode ->
            // iOS automatically hides UI in portrait mode
        },
        onCloseApplication = {
            // unnecessary for iOS
        }
    )
}