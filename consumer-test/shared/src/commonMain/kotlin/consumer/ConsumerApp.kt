package consumer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.di.foundationCoreModule
import bridge.supplies.foundation.core.settings.BooleanSetting
import bridge.supplies.foundation.core.settings.settingName
import bridge.supplies.foundation.ui.foundationUiModule
import bridge.supplies.foundation.ui.settings.FoundationSettingsScreen
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.FoundationTab
import bridge.supplies.foundation.ui.shell.Landing
import bridge.supplies.foundation.ui.shell.RouteRegistry
import bridge.supplies.foundation.ui.shell.screenMetadata
import bridge.supplies.foundation.ui.theme.ColorTheme
import kotlinx.serialization.Serializable
import org.koin.core.module.Module

// A minimal app on Foundation's public API. It only has to compile and link on every target.

@Serializable data object Start : NavKey
@Serializable data class Detail(val id: Int) : NavKey
@Serializable data object Options : NavKey

val consumerRoutes = RouteRegistry {
    subclass(Start::class, Start.serializer())
    subclass(Detail::class, Detail.serializer())
    subclass(Options::class, Options.serializer())
}

// Foundation's own strings stand in for app strings here
val consumerTabs = listOf(
    FoundationTab("start", ColorTheme.RED.titleRes, Icons.Outlined.Home, Icons.Filled.Home, Start),
    FoundationTab("options", ColorTheme.BLUE.titleRes, Icons.Outlined.Settings, Icons.Filled.Settings, Options),
)

val consumerSetting = BooleanSetting(settingName("consumer_flag"), false)

fun consumerModules(): List<Module> = listOf(
    foundationCoreModule(
        FoundationConfig(
            appName = "Consumer",
            packageName = "bridge.supplies.consumertest",
            version = "0.0.1",
            build = "1",
            encryptionKey = "ConsumerTestKey!",
        )
    ),
    foundationUiModule(),
)

@Composable
fun ConsumerApp() {
    FoundationApp(
        tabs = consumerTabs,
        routes = consumerRoutes,
        landing = Landing.None,
    ) {
        entry<Start> { Text("Start") }
        entry<Detail>(metadata = screenMetadata(titleText = "Detail")) { route -> Text("Detail ${route.id}") }
        entry<Options> {
            FoundationSettingsScreen {
                section(ColorTheme.GREEN.titleRes) {
                    switch(consumerSetting, ColorTheme.OFF.titleRes)
                }
            }
        }
    }
}
