package bridge.supplies.foundation

import KoinInitializer
import android.app.Application

class Foundation : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        KoinInitializer(applicationContext).init()
    }
    
}