package bridge.supplies.foundation.demo.config

import bridge.supplies.foundation.BuildConfig
import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.di.foundationCoreModule
import bridge.supplies.foundation.ui.foundationUiModule
import org.koin.core.module.Module
import org.koin.dsl.module

// Koin modules

expect class KoinInitializer {
    fun init()
}

/** Foundation configuration for this app (identity from the generated BuildConfig). */
val foundationConfig = FoundationConfig(
    appName = BuildConfig.APP_NAME,
    packageName = BuildConfig.PACKAGE_NAME,
    version = BuildConfig.APP_VERSION,
    build = BuildConfig.APP_BUILD,
    encryptionKey = BuildConfig.ENCRYPTION_KEY,
    shareUrl = "https://github.com/Bridge-Supplies/Foundation-KMP-Compose",
)

/** Foundation's settings, platform and shared state. */
val foundationModule: Module = module {
    includes(foundationCoreModule(foundationConfig), foundationUiModule())
}

expect val viewModelModule: Module
