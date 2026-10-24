package bridge.supplies.foundation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import bridge.supplies.foundation.demo.config.showSystemUI
import bridge.supplies.foundation.demo.data.MainViewModel
import bridge.supplies.foundation.demo.ui.App
import bridge.supplies.foundation.ui.util.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            val viewModel = koinViewModel<MainViewModel>()
            
            App(
                viewModel = viewModel,
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