package bridge.supplies.foundation.core.di

import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.crypto.FoundationQrCodec
import bridge.supplies.foundation.core.crypto.QrCodec
import bridge.supplies.foundation.core.platform.FoundationPlatform
import bridge.supplies.foundation.core.platform.currentPlatform
import bridge.supplies.foundation.core.sheets.DefaultSheetController
import bridge.supplies.foundation.core.sheets.SheetController
import bridge.supplies.foundation.core.settings.DataStoreSettings
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.core.storage.createSettingsDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module providing [FoundationConfig], [FoundationPlatform], the shared settings DataStore, [FoundationSettings], [QrCodec] and [SheetController]. */
public fun foundationCoreModule(config: FoundationConfig): Module = module {
    single { config }
    single { createSettingsDataStore(config) }
    single<FoundationSettings> { DataStoreSettings(get()) }
    single<QrCodec> { FoundationQrCodec(config.encryptionKey) }
    single<FoundationPlatform> { currentPlatform() }
    single<SheetController> { DefaultSheetController() }
}
