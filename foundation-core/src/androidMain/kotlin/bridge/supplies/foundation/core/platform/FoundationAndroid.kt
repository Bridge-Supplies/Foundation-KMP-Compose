package bridge.supplies.foundation.core.platform

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

/** Holds the Android application context. The app calls [init] once at startup. */
public object FoundationAndroid {
    public lateinit var appContext: Context
        private set

    public fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val backCameraPresent by lazy {
        runCatching {
                val cameras = appContext.getSystemService(CameraManager::class.java)
                cameras.cameraIdList.any { id ->
                    cameras.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK
                }
            }
            .getOrDefault(false)
    }

    /**
     * Whether the device has a camera facing away from the screen. Assumes so until the app has
     * called [init].
     */
    internal val hasBackCamera: Boolean
        get() = !::appContext.isInitialized || backCameraPresent
}
