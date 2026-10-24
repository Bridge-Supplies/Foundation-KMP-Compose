package bridge.supplies.foundation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.crypto.FoundationQrCodec
import bridge.supplies.foundation.core.crypto.QrCodec
import bridge.supplies.foundation.core.platform.FoundationPlatform
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.core.platform.PlatformType
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.core.settings.SettingKey
import bridge.supplies.foundation.core.sheets.DefaultSheetController
import bridge.supplies.foundation.core.sheets.SheetController
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.app_settings_title
import bridge.supplies.foundation.ui.resources.theme_settings_title
import bridge.supplies.foundation.ui.shell.FoundationTab
import bridge.supplies.foundation.ui.shell.RouteRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.koin.core.module.Module
import org.koin.dsl.module

class InMemoryFoundationSettings(
    initialValues: Map<SettingKey<*>, Any?> = emptyMap()
) : FoundationSettings {
    private val values = MutableStateFlow(initialValues)

    @Suppress("UNCHECKED_CAST")
    override fun <T> flow(key: SettingKey<T>): Flow<T> =
        values.map { map -> (map[key] as? T) ?: key.default }

    override suspend fun <T> set(key: SettingKey<T>, value: T) {
        values.update { it + (key to value) }
    }

    override suspend fun remove(key: SettingKey<*>) {
        values.update { it - key }
    }
}

class FakeWindowInfo(
    override val isWindowFocused: Boolean = true,
    override val containerSize: IntSize
) : WindowInfo

class FakeFoundationPlatform(
    override val type: PlatformType = PlatformType.DESKTOP,
    override val name: String = "Desktop Test",
    override val features: Set<PlatformFeature> = setOf(
        PlatformFeature.DYNAMIC_COLORS,
        PlatformFeature.VIBRATION,
        PlatformFeature.FULLSCREEN_LANDSCAPE
    )
) : FoundationPlatform

fun testFoundationModule(
    settings: FoundationSettings = InMemoryFoundationSettings(),
    config: FoundationConfig = FoundationConfig(
        appName = "Foundation UI Tests",
        packageName = "bridge.supplies.foundation.ui.test",
        version = "2.0.0",
        build = "1",
        encryptionKey = "1234567890123456",
    ),
    platform: FoundationPlatform = FakeFoundationPlatform()
): Module = module {
    single { config }
    single { settings }
    single { platform }
    single<QrCodec> { FoundationQrCodec(config.encryptionKey) }
    single<SheetController> { DefaultSheetController() }
    single { FoundationViewModel(get(), get(), get()) }
}

// Shell test routes
@Serializable
data object Tab1RootRoute : NavKey

@Serializable
data object Tab1DetailRoute : NavKey

@Serializable
data object Tab2RootRoute : NavKey

@Serializable
data object Tab3ImmersiveRoute : NavKey

val testRouteRegistry = RouteRegistry {
    subclass(Tab1RootRoute::class, Tab1RootRoute.serializer())
    subclass(Tab1DetailRoute::class, Tab1DetailRoute.serializer())
    subclass(Tab2RootRoute::class, Tab2RootRoute.serializer())
    subclass(Tab3ImmersiveRoute::class, Tab3ImmersiveRoute.serializer())
}

val testTabs = listOf(
    FoundationTab(
        id = "tab1",
        title = Res.string.theme_settings_title,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        root = Tab1RootRoute
    ),
    FoundationTab(
        id = "tab2",
        title = Res.string.app_settings_title,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        root = Tab2RootRoute
    ),
    FoundationTab(
        id = "tab3",
        title = Res.string.theme_settings_title,
        icon = Icons.Outlined.Info,
        selectedIcon = Icons.Filled.Info,
        root = Tab3ImmersiveRoute
    )
)
