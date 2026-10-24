package bridge.supplies.foundation.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Typed access to the app's single shared settings file (Foundation's and the app's own keys). */
public interface FoundationSettings {
    public fun <T> flow(key: SettingKey<T>): Flow<T>
    public suspend fun <T> get(key: SettingKey<T>): T = flow(key).first()
    public suspend fun <T> set(key: SettingKey<T>, value: T)
    public suspend fun remove(key: SettingKey<*>)
}

/** [FoundationSettings] backed by a Preferences [DataStore]. DataStore allows one instance per file, so share this. */
public class DataStoreSettings(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : FoundationSettings {
    
    override fun <T> flow(key: SettingKey<T>): Flow<T> =
        dataStore.data.map { preferences -> read(preferences, key) }
    
    override suspend fun <T> set(key: SettingKey<T>, value: T) {
        dataStore.edit { preferences -> write(preferences, key, value) }
    }
    
    override suspend fun remove(key: SettingKey<*>) {
        dataStore.edit { preferences -> preferences.remove(preferencesKey(key)) }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> read(preferences: Preferences, key: SettingKey<T>): T = when (key) {
        is BooleanSetting -> preferences[booleanPreferencesKey(key.name)] ?: key.default
        is IntSetting -> preferences[intPreferencesKey(key.name)] ?: key.default
        is LongSetting -> preferences[longPreferencesKey(key.name)] ?: key.default
        is StringSetting -> preferences[stringPreferencesKey(key.name)] ?: key.default
        is StringSetSetting -> preferences[stringSetPreferencesKey(key.name)] ?: key.default
        is EnumSetting<*> -> preferences[intPreferencesKey(key.name)]?.let { key.entries.getOrNull(it) } ?: key.default
        is JsonSetting -> preferences[stringPreferencesKey(key.name)]
            ?.let { runCatching { json.decodeFromString(key.serializer, it) }.getOrNull() }
            ?: key.default
    } as T
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> write(preferences: androidx.datastore.preferences.core.MutablePreferences, key: SettingKey<T>, value: T) {
        when (key) {
            is BooleanSetting -> preferences[booleanPreferencesKey(key.name)] = value as Boolean
            is IntSetting -> preferences[intPreferencesKey(key.name)] = value as Int
            is LongSetting -> preferences[longPreferencesKey(key.name)] = value as Long
            is StringSetting -> preferences[stringPreferencesKey(key.name)] = value as String
            is StringSetSetting -> preferences[stringSetPreferencesKey(key.name)] = value as Set<String>
            is EnumSetting<*> -> preferences[intPreferencesKey(key.name)] = (value as Enum<*>).ordinal
            is JsonSetting -> preferences[stringPreferencesKey(key.name)] = json.encodeToString(key.serializer, value)
        }
    }
    
    private fun preferencesKey(key: SettingKey<*>): Preferences.Key<*> = when (key) {
        is BooleanSetting -> booleanPreferencesKey(key.name)
        is IntSetting, is EnumSetting<*> -> intPreferencesKey(key.name)
        is LongSetting -> longPreferencesKey(key.name)
        is StringSetting, is JsonSetting -> stringPreferencesKey(key.name)
        is StringSetSetting -> stringSetPreferencesKey(key.name)
    }
}
