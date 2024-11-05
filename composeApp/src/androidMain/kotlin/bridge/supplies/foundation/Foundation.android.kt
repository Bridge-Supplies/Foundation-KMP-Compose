package bridge.supplies.foundation

import android.app.Application
import config.KoinInitializer
import config.dataStoreModule
import config.platformModule
import config.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class Foundation : Application() {
    
    companion object {
        lateinit var instance: Foundation
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@Foundation)
            // Load modules
            modules(
                platformModule,
                viewModelModule,
                dataStoreModule
            )
        }
    }
    
}