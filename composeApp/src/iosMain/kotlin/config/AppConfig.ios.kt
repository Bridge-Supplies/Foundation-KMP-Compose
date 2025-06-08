package config

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import data.DataRepository
import data.MainViewModel
import data.dataStore
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.UIKit.UIDevice

// System info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.IOS
    override val name = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val version = platform.Foundation.NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: ""
    override val build = platform.Foundation.NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: ""
    override val shareUrl = "https://github.com/Bridge-Supplies/Foundation-KMP-Compose"
    override val supportedFeatures = listOf(
        Feature.VIBRATION,
        Feature.CODE_SCANNING
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun isPortraitMode(): Boolean {
    val config = LocalWindowInfo.current.containerSize
    val pxHeight = config.height.toFloat()
    val pxWidth = config.width.toFloat()
    return (pxWidth / pxHeight) <= 1f
}


// Koin modules

actual class KoinInitializer() {
    actual fun init() {
        startKoin {
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
    singleOf(::MainViewModel)
}

actual val dataStoreModule = module {
    single { DataRepository(dataStore()) }
}