package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bridge.supplies.foundation.core.storage.createDataStore
import bridge.supplies.foundation.core.storage.dataStoreFileName

fun dataStore(): DataStore<Preferences> = createDataStore(
    producePath = { dataStoreFileName }
)