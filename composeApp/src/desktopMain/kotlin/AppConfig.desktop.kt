import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Platform info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.DESKTOP
    override val name = "Java ${System.getProperty("java.version")}"
    override val supportsDynamicColors = false
    override val supportsVibration = false
    override val supportsQrGeneration = true
    override val supportsQrScanning = false
}


// Koin modules

actual class KoinInitializer() {
    actual fun init() {
        startKoin {
            modules(appModule, viewModelModule, dataStoreModule)
        }
    }
}

actual val appModule = module {
    single { "Hello Desktop world!" }
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