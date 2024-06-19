package config

import android.content.Context
import android.os.Build
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.BuildConfig
import data.DataRepository
import data.MainViewModel
import data.dataStore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

// System info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.ANDROID
    override val name = "Android ${Build.VERSION.SDK_INT}"
    override val version = BuildConfig.APP_VERSION
    override val build = BuildConfig.APP_BUILD
    override val landscapeContentPadding: Dp = 32.dp
    override val supportedFeatures: List<Feature>
        get() {
            val features = mutableListOf(
                Feature.FULLSCREEN_LANDSCAPE,
                Feature.VIBRATION,
                Feature.CODE_SCANNING,
                Feature.CODE_UPLOADING
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                features.add(Feature.DYNAMIC_COLORS)
            }
            
            return features
        }
}

@Composable
actual fun getScreenSizeInfo(): ScreenSizeInfo {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val dpHeight = config.screenHeightDp.dp
    val dpWidth = config.screenWidthDp.dp
    
    return remember(density, config) {
        ScreenSizeInfo(
            pxHeight = with(density) { dpHeight.roundToPx() },
            pxWidth = with(density) { dpWidth.roundToPx() },
            dpHeight = dpHeight,
            dpWidth = dpWidth
        )
    }
}

@Composable
actual fun isPortraitMode(): Boolean {
    val config = LocalConfiguration.current
    val dpHeight = config.screenHeightDp.toFloat()
    val dpWidth = config.screenWidthDp.toFloat()
    return (dpWidth / dpHeight) <= 1f
}

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
        startKoin {
            androidContext(context)
            androidLogger()
            modules(
                platformModule,
                viewModelModule,
                dataStoreModule
            )
        }
    }
}

actual val platformModule = module {
    single { getPlatform() }
}

actual val viewModelModule = module {
    viewModelOf(::MainViewModel)
}

actual val dataStoreModule = module {
    single { DataRepository(dataStore(get())) }
}