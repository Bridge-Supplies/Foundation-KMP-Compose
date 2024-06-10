package bridge.supplies.foundation

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import showSystemUI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            App() { isPortrait ->
                showSystemUI(isPortrait)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}