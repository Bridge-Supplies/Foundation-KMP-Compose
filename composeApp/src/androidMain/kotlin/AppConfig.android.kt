import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Platform info

actual fun getPlatform() = object : Platform {
    override val type = PlatformType.ANDROID
    override val name = "Android ${Build.VERSION.SDK_INT}"
    override val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    override val supportsVibration = true
    override val supportsQrGeneration = true
    override val supportsQrScanning = true
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
                appModule,
                viewModelModule,
                dataStoreModule
            )
        }
    }
}

actual val appModule = module {
    single { "Hello Google world!" }
}

actual val viewModelModule = module {
    viewModelOf(::MainViewModel)
}

actual val dataStoreModule = module {
    single { DataRepository(dataStore(get())) }
}


// Code scanning

actual fun bitmapFromBytes(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}