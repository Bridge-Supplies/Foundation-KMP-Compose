package data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import bridge.supplies.foundation.core.storage.createDataStore
import bridge.supplies.foundation.core.storage.dataStoreFileName

fun dataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)