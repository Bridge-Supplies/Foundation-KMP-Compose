package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.platform.FoundationAndroid

internal actual fun settingsFilePath(config: FoundationConfig): String =
    FoundationAndroid.appContext.filesDir.resolve(config.storage.fileName).absolutePath
