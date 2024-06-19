package config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.BuildConfig
import data.DataRepository
import data.MainViewModel
import data.dataStore
import org.jetbrains.skia.Image
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// System info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.DESKTOP
    override val name = "Java ${System.getProperty("java.version")}"
    override val version = BuildConfig.APP_VERSION
    override val build = BuildConfig.APP_BUILD
    override val landscapeContentPadding: Dp = 16.dp
    override val supportedFeatures = listOf<Feature>()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenSizeInfo(): ScreenSizeInfo {
    val density = LocalDensity.current
    val config = LocalWindowInfo.current.containerSize
    val pxHeight = config.height
    val pxWidth = config.width
    
    return remember(density, config) {
        ScreenSizeInfo(
            pxHeight = pxHeight,
            pxWidth = pxWidth,
            dpHeight = with(density) { pxHeight.toDp() },
            dpWidth = with(density) { pxWidth.toDp() }
        )
    }
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
            modules(platformModule, viewModelModule, dataStoreModule)
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