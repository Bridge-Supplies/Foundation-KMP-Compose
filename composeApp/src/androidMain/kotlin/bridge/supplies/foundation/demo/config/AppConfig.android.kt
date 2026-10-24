package bridge.supplies.foundation.demo.config

import bridge.supplies.foundation.core.platform.FoundationAndroid
import android.content.Context
import android.os.Build
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import bridge.supplies.foundation.demo.data.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun ComponentActivity.showSystemUI(show: Boolean) {
    if (show) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                show(android.view.WindowInsets.Type.statusBars())
                show(android.view.WindowInsets.Type.navigationBars())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            }
        }
    } else {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(android.view.WindowInsets.Type.statusBars())
                hide(android.view.WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}


// Koin modules

actual class KoinInitializer(
    private val context: Context
) {
    actual fun init() {
        FoundationAndroid.init(context)
        startKoin {
            androidContext(context)
            androidLogger()
            modules(
                viewModelModule,
                foundationModule
            )
        }
    }
}

actual val viewModelModule = module {
    viewModelOf(::MainViewModel)
}
