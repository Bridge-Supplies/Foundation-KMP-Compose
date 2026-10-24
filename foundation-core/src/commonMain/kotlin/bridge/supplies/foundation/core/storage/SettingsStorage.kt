package bridge.supplies.foundation.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bridge.supplies.foundation.core.config.FoundationConfig

/** Absolute path of the shared settings file for this platform (see [bridge.supplies.foundation.core.config.StorageConfig]). */
internal expect fun settingsFilePath(config: FoundationConfig): String

/** The app's single settings [DataStore]. Create it once and share it. */
internal fun createSettingsDataStore(config: FoundationConfig): DataStore<Preferences> =
    createDataStore(producePath = { settingsFilePath(config) })
