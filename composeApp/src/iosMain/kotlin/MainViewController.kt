import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController(
    configure = {
        KoinInitializer().init()
    }
) {
    App() { isPortraitMode ->
        // iOS automatically hides UI in portrait mode
    }
}