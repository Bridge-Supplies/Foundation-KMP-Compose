package bridge.supplies.foundation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import config.showSystemUI
import screens.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                onShowSystemUi = { isPortraitMode ->
                    showSystemUI(isPortraitMode)
                },
                onCloseApplication = {
                    finish()
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}