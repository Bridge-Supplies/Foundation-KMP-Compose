package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.getPlatform
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
    FULLSCREEN_LANDSCAPE(
        "${SETTINGS_KEY}_fullscreen_landscape",
        true
    ),
    COLOR_THEME(
        "${SETTINGS_KEY}_color_theme",
        if (getPlatform().supportsFeature(Feature.DYNAMIC_COLORS)) ColorTheme.AUTO else ColorTheme.OFF
    ),
    PALETTE_STYLE(
        "${SETTINGS_KEY}_palette_style",
        Palette.TONAL
    ),
    DARK_MODE(
        "${SETTINGS_KEY}_dark_mode",
        DarkMode.AUTO
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
        private val FULLSCREEN_LANDSCAPE_KEY = booleanPreferencesKey(Prefs.FULLSCREEN_LANDSCAPE.key)
        private val COLOR_THEME_KEY = intPreferencesKey(Prefs.COLOR_THEME.key)
        private val PALETTE_STYLE_KEY = intPreferencesKey(Prefs.PALETTE_STYLE.key)
        private val DARK_MODE_KEY = intPreferencesKey(Prefs.DARK_MODE.key)
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
    
    // FULLSCREEN LANDSCAPE
    suspend fun setFullscreenLandscape(fullscreenLandscape: Boolean) {
        dataStore.edit { preferences ->
            preferences[FULLSCREEN_LANDSCAPE_KEY] = fullscreenLandscape
        }
    }
    
    fun getFullscreenLandscapeFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[FULLSCREEN_LANDSCAPE_KEY] ?: Prefs.FULLSCREEN_LANDSCAPE.defaultValue as Boolean
        }
    
    // COLOR THEMING (Dynamic colors available on Android 12+)
    suspend fun setColorTheme(colorTheme: ColorTheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_THEME_KEY] = colorTheme.ordinal
        }
    }
    
    fun getColorThemeFlow(): Flow<ColorTheme> =
        dataStore.data.map { preferences ->
            val colorTheme = preferences[COLOR_THEME_KEY]
            if (colorTheme != null) {
                try {
                    ColorTheme.entries[colorTheme]
                } catch (e: Exception) {
                    Prefs.COLOR_THEME.defaultValue as ColorTheme
                }
            } else {
                Prefs.COLOR_THEME.defaultValue as ColorTheme
            }
        }
    
    // PALETTE STYLE
    suspend fun setPaletteStyle(palette: Palette) {
        dataStore.edit { preferences ->
            preferences[PALETTE_STYLE_KEY] = palette.ordinal
        }
    }
    
    fun getPaletteStyleFlow(): Flow<Palette> =
        dataStore.data.map { preferences ->
            val palette = preferences[PALETTE_STYLE_KEY]
            if (palette != null) {
                try {
                    Palette.entries[palette]
                } catch (e: Exception) {
                    Prefs.PALETTE_STYLE.defaultValue as Palette
                }
            } else {
                Prefs.PALETTE_STYLE.defaultValue as Palette
            }
        }
    
    // DARK MODE
    suspend fun setDarkMode(darkMode: DarkMode) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode.ordinal
        }
    }
    
    fun getDarkModeFlow(): Flow<DarkMode> =
        dataStore.data.map { preferences ->
            val darkMode = preferences[DARK_MODE_KEY]
            if (darkMode != null) {
                try {
                    DarkMode.entries[darkMode]
                } catch (e: Exception) {
                    Prefs.DARK_MODE.defaultValue as DarkMode
                }
            } else {
                Prefs.DARK_MODE.defaultValue as DarkMode
            }
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