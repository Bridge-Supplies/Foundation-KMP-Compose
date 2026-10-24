package bridge.supplies.foundation.core.platform

import android.content.Context

/** Holds the Android application context. The app calls [init] once at startup. */
public object FoundationAndroid {
    public lateinit var appContext: Context
        private set
    
    public fun init(context: Context) {
        appContext = context.applicationContext
    }
}
