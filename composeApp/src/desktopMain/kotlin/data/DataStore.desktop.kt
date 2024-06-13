package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun dataStore(): DataStore<Preferences> = createDataStore(
    producePath = { dataStoreFileName }
)