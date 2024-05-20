import androidx.compose.ui.graphics.ImageBitmap
import org.koin.core.module.Module

// Platform info

enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP
}

interface Platform {
    val type: PlatformType
    val name: String
    val supportsDynamicColors: Boolean
    val supportsVibration: Boolean
    val supportsQrGeneration: Boolean
    val supportsQrScanning: Boolean
}

expect fun getPlatform(): Platform


// Koin modules

expect class KoinInitializer {
    fun init()
}

expect val appModule: Module

expect val dataStoreModule: Module

expect val viewModelModule: Module


// Code scanning

expect fun bitmapFromBytes(bytes: ByteArray): ImageBitmap