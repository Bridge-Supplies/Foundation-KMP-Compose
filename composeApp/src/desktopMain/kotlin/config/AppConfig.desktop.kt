package config

import data.MainViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// Koin modules

actual class KoinInitializer() {
    actual fun init() {
        startKoin {
            modules(viewModelModule, foundationModule)
        }
    }
}

actual val viewModelModule = module {
    singleOf(::MainViewModel)
}
