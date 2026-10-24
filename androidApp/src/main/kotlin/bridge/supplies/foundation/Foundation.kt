package bridge.supplies.foundation

import android.app.Application
import config.KoinInitializer

class Foundation : Application() {
    
    companion object {
        lateinit var instance: Foundation
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        KoinInitializer(applicationContext).init()
    }
    
}