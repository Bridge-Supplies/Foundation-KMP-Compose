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
import config.PlatformType
import config.getPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val SETTINGS_KEY = "Settings"

sealed class StoredValue {
    // FOUNDATION
    
    data class DESKTOP_RESOLUTION_X(
        val key: Preferences.Key<Int> = intPreferencesKey("${SETTINGS_KEY}_desktop_resolution_x"),
        val default: Int = 800
    ) : StoredValue()
    
    data class DESKTOP_RESOLUTION_Y(
        val key: Preferences.Key<Int> = intPreferencesKey("${SETTINGS_KEY}_desktop_resolution_y"),
        val default: Int = 480
    ) : StoredValue()
    
    data class LANDING_TIPS(
        val key: Preferences.Key<Boolean> = booleanPreferencesKey("${SETTINGS_KEY}_landing_tips"),
        val default: Boolean = true
    ) : StoredValue()
    
    data class FULLSCREEN_LANDSCAPE(
        val key: Preferences.Key<Boolean> = booleanPreferencesKey("${SETTINGS_KEY}_fullscreen_landscape"),
        val default: Boolean = false
    ) : StoredValue()
    
    data class COLOR_THEME(
        val key: Preferences.Key<Int> = intPreferencesKey("${SETTINGS_KEY}_color_theme"),
        val default: ColorTheme = if (getPlatform().supportsFeature(Feature.DYNAMIC_COLORS))
            ColorTheme.AUTO else ColorTheme.OFF
    ) : StoredValue()
    
    data class PALETTE_STYLE(
        val key: Preferences.Key<Int> = intPreferencesKey("${SETTINGS_KEY}_palette_style"),
        val default: Palette = Palette.TINTED
    ) : StoredValue()
    
    data class DARK_MODE(
        val key: Preferences.Key<Int> = intPreferencesKey("${SETTINGS_KEY}_dark_mode"),
        val default: DarkMode = DarkMode.AUTO
    ) : StoredValue()
    
    data class VIBRATION(
        val key: Preferences.Key<Boolean> = booleanPreferencesKey("${SETTINGS_KEY}_vibration"),
        val default: Boolean = true
    ) : StoredValue()
    
    data class SCROLL_HELPERS(
        val key: Preferences.Key<Boolean> = booleanPreferencesKey("${SETTINGS_KEY}_scroll_helpers"),
        val default: Boolean = getPlatform().type == PlatformType.DESKTOP
    )
    
    data class ENCRYPTED_SHARE(
        val key: Preferences.Key<Boolean> = booleanPreferencesKey("${SETTINGS_KEY}_encrypted_share"),
        val default: Boolean = true
    ) : StoredValue()
}


class DataRepository(
    private val dataStore: DataStore<Preferences>
) {
    // FOUNDATION
    
    // DESKTOP RESOLUTION X
    suspend fun setDesktopResolutionX(desktopResolutionX: Int) {
        dataStore.edit { preferences ->
            val pref = StoredValue.DESKTOP_RESOLUTION_X()
            preferences[pref.key] = desktopResolutionX
        }
    }
    
    fun getDesktopResolutionXFlow(): Flow<Int> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.DESKTOP_RESOLUTION_X()
            preferences[pref.key] ?: pref.default
        }
    
    // DESKTOP RESOLUTION Y
    suspend fun setDesktopResolutionY(desktopResolutionY: Int) {
        dataStore.edit { preferences ->
            val pref = StoredValue.DESKTOP_RESOLUTION_Y()
            preferences[pref.key] = desktopResolutionY
        }
    }
    
    fun getDesktopResolutionYFlow(): Flow<Int> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.DESKTOP_RESOLUTION_Y()
            preferences[pref.key] ?: pref.default
        }
    
    // LANDING TIPS
    suspend fun setLandingTips(landingTips: Boolean) {
        dataStore.edit { preferences ->
            val pref = StoredValue.LANDING_TIPS()
            preferences[pref.key] = landingTips
        }
    }
    
    fun getLandingTipsFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.LANDING_TIPS()
            preferences[pref.key] ?: pref.default
        }
    
    // FULLSCREEN LANDSCAPE
    suspend fun setFullscreenLandscape(fullscreenLandscape: Boolean) {
        dataStore.edit { preferences ->
            val pref = StoredValue.FULLSCREEN_LANDSCAPE()
            preferences[pref.key] = fullscreenLandscape
        }
    }
    
    fun getFullscreenLandscapeFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.FULLSCREEN_LANDSCAPE()
            preferences[pref.key] ?: pref.default
        }
    
    // COLOR THEMING (Dynamic colors available on Android 12+)
    suspend fun setColorTheme(colorTheme: ColorTheme) {
        dataStore.edit { preferences ->
            val pref = StoredValue.COLOR_THEME()
            preferences[pref.key] = colorTheme.ordinal
        }
    }
    
    fun getColorThemeFlow(): Flow<ColorTheme> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.COLOR_THEME()
            val colorTheme = preferences[pref.key]
            if (colorTheme != null) {
                try {
                    ColorTheme.entries[colorTheme]
                } catch (e: Exception) {
                    pref.default
                }
            } else {
                pref.default
            }
        }
    
    // PALETTE STYLE
    suspend fun setPaletteStyle(palette: Palette) {
        dataStore.edit { preferences ->
            val pref = StoredValue.PALETTE_STYLE()
            preferences[pref.key] = palette.ordinal
        }
    }
    
    fun getPaletteStyleFlow(): Flow<Palette> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.PALETTE_STYLE()
            val palette = preferences[pref.key]
            if (palette != null) {
                try {
                    Palette.entries[palette]
                } catch (e: Exception) {
                    pref.default
                }
            } else {
                pref.default
            }
        }
    
    // DARK MODE
    suspend fun setDarkMode(darkMode: DarkMode) {
        dataStore.edit { preferences ->
            val pref = StoredValue.DARK_MODE()
            preferences[pref.key] = darkMode.ordinal
        }
    }
    
    fun getDarkModeFlow(): Flow<DarkMode> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.DARK_MODE()
            val darkMode = preferences[pref.key]
            if (darkMode != null) {
                try {
                    DarkMode.entries[darkMode]
                } catch (e: Exception) {
                    pref.default
                }
            } else {
                pref.default
            }
        }
    
    // VIBRATION
    suspend fun setVibration(vibration: Boolean) {
        dataStore.edit { preferences ->
            val pref = StoredValue.VIBRATION()
            preferences[pref.key] = vibration
        }
    }
    
    fun getVibrationFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.VIBRATION()
            preferences[pref.key] ?: pref.default
        }
    
    // SCROLL HELPERS
    suspend fun setScrollHelpers(scrollHelpers: Boolean) {
        dataStore.edit { preferences ->
            val pref = StoredValue.SCROLL_HELPERS()
            preferences[pref.key] = scrollHelpers
        }
    }
    
    fun getScrollHelpersFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.SCROLL_HELPERS()
            preferences[pref.key] ?: pref.default
        }
    
    // ENCRYPTED SHARE
    suspend fun setEncryptedShare(encryptedShare: Boolean) {
        dataStore.edit { preferences ->
            val pref = StoredValue.ENCRYPTED_SHARE()
            preferences[pref.key] = encryptedShare
        }
    }
    
    fun getEncryptedShareFlow(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            val pref = StoredValue.ENCRYPTED_SHARE()
            preferences[pref.key] ?: pref.default
        }
    
}