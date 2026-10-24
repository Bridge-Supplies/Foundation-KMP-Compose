package bridge.supplies.foundation.core.settings

import kotlinx.serialization.KSerializer

/**
 * A typed key in Foundation's shared settings file. [name] is the exact stored key: never rename it for an
 * existing app (write a migration instead), or users lose that setting.
 */
public sealed interface SettingKey<T> {
    public val name: String
    public val default: T
}

public class BooleanSetting(override val name: String, override val default: Boolean) : SettingKey<Boolean>
public class IntSetting(override val name: String, override val default: Int) : SettingKey<Int>
public class LongSetting(override val name: String, override val default: Long) : SettingKey<Long>
public class StringSetting(override val name: String, override val default: String) : SettingKey<String>
public class StringSetSetting(override val name: String, override val default: Set<String>) : SettingKey<Set<String>>

/**
 * Stored as the enum's ordinal (an Int), so **never reorder or remove [entries]**; only append.
 * An unknown stored ordinal reads as [default].
 */
public class EnumSetting<E : Enum<E>>(
    override val name: String,
    override val default: E,
    public val entries: List<E>,
) : SettingKey<E>

/** Stored as a JSON string. A value that fails to decode reads as [default]. */
public class JsonSetting<T>(
    override val name: String,
    override val default: T,
    public val serializer: KSerializer<T>,
) : SettingKey<T>

/** Key name with the legacy `Settings_` prefix used by Foundation 1.x apps, e.g. `settingName("dark_mode")`. */
public fun settingName(suffix: String): String = "Settings_$suffix"
