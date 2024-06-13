package bridge.supplies.foundation

import android.app.Application
import config.KoinInitializer

class Foundation : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        KoinInitializer(applicationContext).init()
    }
    
}