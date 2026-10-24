package bridge.supplies.foundation.core.settings

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import bridge.supplies.foundation.core.storage.createDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreSettingsTests {
    
    private enum class Mode { AUTO, LIGHT, DARK }
    
    @Serializable
    private data class User(val name: String, val birthday: Long)
    
    private fun newStore() = createDataStore {
        File.createTempFile("settings", ".preferences_pb").apply { delete(); deleteOnExit() }.absolutePath
    }
    
    @Test
    fun roundTripsEveryType() = runBlocking {
        val settings = DataStoreSettings(newStore())
        val bool = BooleanSetting(settingName("bool"), false)
        val int = IntSetting(settingName("int"), 1)
        val long = LongSetting(settingName("long"), 2L)
        val string = StringSetting(settingName("string"), "")
        val set = StringSetSetting(settingName("set"), emptySet())
        val enum = EnumSetting(settingName("enum"), Mode.AUTO, Mode.entries)
        val json = JsonSetting(settingName("json"), User("", 0), User.serializer())
        
        assertEquals(false, settings.get(bool))
        assertEquals(Mode.AUTO, settings.get(enum))
        
        settings.set(bool, true)
        settings.set(int, 42)
        settings.set(long, 123L)
        settings.set(string, "hello")
        settings.set(set, setOf("a", "b"))
        settings.set(enum, Mode.DARK)
        settings.set(json, User("Ra", 7L))
        
        assertEquals(true, settings.get(bool))
        assertEquals(42, settings.get(int))
        assertEquals(123L, settings.get(long))
        assertEquals("hello", settings.get(string))
        assertEquals(setOf("a", "b"), settings.get(set))
        assertEquals(Mode.DARK, settings.get(enum))
        assertEquals(User("Ra", 7L), settings.get(json))
        
        settings.remove(int)
        assertEquals(1, settings.get(int))
    }
    
    @Test
    fun readsValuesWrittenWithLegacyRawKeys() = runBlocking {
        // Foundation 1.x wrote enums as Int ordinals under "Settings_<name>"
        val store = newStore()
        store.edit { it[intPreferencesKey("Settings_dark_mode")] = 2 }
        val settings = DataStoreSettings(store)
        assertEquals(Mode.DARK, settings.get(EnumSetting(settingName("dark_mode"), Mode.AUTO, Mode.entries)))
    }
    
    @Test
    fun unknownOrdinalOrBadJsonFallsBackToDefault() = runBlocking {
        val store = newStore()
        store.edit {
            it[intPreferencesKey("Settings_enum")] = 99
            it[stringPreferencesKey("Settings_json")] = "{not json"
        }
        val settings = DataStoreSettings(store)
        assertEquals(Mode.LIGHT, settings.get(EnumSetting(settingName("enum"), Mode.LIGHT, Mode.entries)))
        assertEquals(User("x", 1), settings.get(JsonSetting(settingName("json"), User("x", 1), User.serializer())))
    }
}
