package bridge.supplies.foundation.core.di

import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.settings.DataStoreSettings
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.core.storage.createSettingsDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module providing [FoundationConfig], the shared settings DataStore and [FoundationSettings]. */
public fun foundationCoreModule(config: FoundationConfig): Module = module {
    single { config }
    single { createSettingsDataStore(config) }
    single<FoundationSettings> { DataStoreSettings(get()) }
}
