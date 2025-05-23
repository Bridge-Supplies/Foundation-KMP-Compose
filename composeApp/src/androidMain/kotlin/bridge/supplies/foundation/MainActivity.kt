package bridge.supplies.foundation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import config.showSystemUI
import data.MainViewModel
import data.koinViewModel
import org.koin.compose.KoinContext
import ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            KoinContext {
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
}