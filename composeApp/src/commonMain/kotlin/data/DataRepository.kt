package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val SETTINGS_KEY = "Settings"

enum class Prefs(
    val key: String,
    val defaultValue: Any
) {
    ENCRYPTED_SHARE(
        "${SETTINGS_KEY}_encrypted_share",
        true
    ),
    DARK_MODE(
        "${SETTINGS_KEY}_dark_mode",
        -1
    ),
    DYNAMIC_COLORS(
        "${SETTINGS_KEY}_dynamic_colors",
        true
    ),
    VIBRATION(
        "${SETTINGS_KEY}_vibration",
        true
    )
}


class DataRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ENCRYPTED_SHARE_KEY = booleanPreferencesKey(Prefs.ENCRYPTED_SHARE.key)
        private val DARK_MODE_KEY = intPreferencesKey(Prefs.DARK_MODE.key)
        private val DYNAMIC_COLORS_KEY = booleanPreferencesKey(Prefs.DYNAMIC_COLORS.key)
        private val VIBRATION_KEY = booleanPreferencesKey(Prefs.VIBRATION.key)
    }
    
    // ENCRYPTED SHARE (for QR generation/scanning)
    suspend fun setEncryptedShare(encryptedShare: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENCRYPTED_SHARE_KEY] = encryptedShare
        }
    }
    
    fun getEncryptedShareFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ENCRYPTED_SHARE_KEY] ?: Prefs.ENCRYPTED_SHARE.defaultValue as Boolean
        }
    
    // DARK MODE (-1 auto, 0 light, 1 dark)
    suspend fun setDarkMode(darkMode: Int) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode
        }
    }
    
    fun getDarkModeFlow(): Flow<Int> =
        dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: Prefs.DARK_MODE.defaultValue as Int
        }
    
    
    // DYNAMIC COLORS (Android 12+)
    suspend fun setDynamicColors(dynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS_KEY] = dynamicColors
        }
    }
    
    fun getDynamicColorsFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[DYNAMIC_COLORS_KEY] ?: Prefs.DYNAMIC_COLORS.defaultValue as Boolean
        }
    
    
    // VIBRATION
    suspend fun setVibration(vibration: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_KEY] = vibration
        }
    }
    
    fun getVibrationFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[VIBRATION_KEY] ?: Prefs.VIBRATION.defaultValue as Boolean
        }
    
}