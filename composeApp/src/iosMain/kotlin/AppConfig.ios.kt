import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.UIKit.UIDevice

// Platform info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.IOS
    override val name = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val supportsDynamicColors = false
    override val supportsVibration = true
    override val supportsQrGeneration = true
    override val supportsQrScanning = true
}


// Koin modules

actual class KoinInitializer() {
    actual fun init() {
        startKoin {
            modules(
                appModule,
                viewModelModule,
                dataStoreModule
            )
        }
    }
}

actual val appModule = module {
    single { "Hello Apple world!" }
}

actual val viewModelModule = module {
    singleOf(::MainViewModel)
}

actual val dataStoreModule = module {
    single { DataRepository(dataStore()) }
}


// Code scanning

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}