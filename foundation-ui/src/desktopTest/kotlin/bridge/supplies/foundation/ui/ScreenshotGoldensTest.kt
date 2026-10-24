package bridge.supplies.foundation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.ui.settings.BuiltInSettings
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.settings.FoundationSettingsScreen
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.LocalRoute
import bridge.supplies.foundation.ui.shell.LocalTopBarActionHost
import bridge.supplies.foundation.ui.shell.TopBarActionHost
import bridge.supplies.foundation.ui.shell.screenMetadata
import bridge.supplies.foundation.ui.theme.ColorTheme
import bridge.supplies.foundation.ui.theme.DarkMode
import bridge.supplies.foundation.ui.theme.FoundationTheme
import bridge.supplies.foundation.ui.theme.Palette
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test

private data object DummySettingsScreenshotRoute : NavKey

@OptIn(ExperimentalTestApi::class)
class ScreenshotGoldensTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private fun setupTestKoin(darkMode: DarkMode) {
        stopKoin()
        val settings = InMemoryFoundationSettings(
            mapOf(
                FoundationSettingKeys.darkMode to darkMode,
                FoundationSettingKeys.colorTheme to ColorTheme.BLUE,
                FoundationSettingKeys.paletteStyle to Palette.BOLD,
            )
        )
        startKoin {
            modules(testFoundationModule(settings))
        }
    }

    @Composable
    private fun ShellTestContent() {
        FoundationApp(
            tabs = testTabs,
            routes = testRouteRegistry
        ) {
            entry<Tab1RootRoute>(
                metadata = screenMetadata(titleText = "Home")
            ) {
                Column {
                    Text("Home Content")
                }
            }
            entry<Tab2RootRoute>(
                metadata = screenMetadata(titleText = "Settings")
            ) {
                Text("Settings Content")
            }
        }
    }

    @Composable
    private fun SettingsTestContent() {
        val actionHost = remember { TopBarActionHost() }
        CompositionLocalProvider(
            LocalRoute provides DummySettingsScreenshotRoute,
            LocalTopBarActionHost provides actionHost
        ) {
            FoundationTheme {
                FoundationSettingsScreen(
                    builtIns = BuiltInSettings(
                        theme = true,
                        encryptedSharing = true,
                        landingTips = true,
                        expressiveAnimation = true,
                        fullscreenLandscape = true,
                        vibration = true,
                        scrollHelpers = true
                    )
                )
            }
        }
    }

    @Test
    fun shellPortraitLight() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    ShellTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "shell_portrait_light")
    }

    @Test
    fun shellPortraitDark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    ShellTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "shell_portrait_dark")
    }

    @Test
    fun shellLandscapeLight() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(800, 400))
            ) {
                Box(Modifier.size(800.dp, 400.dp)) {
                    ShellTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "shell_landscape_light")
    }

    @Test
    fun shellLandscapeDark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(800, 400))
            ) {
                Box(Modifier.size(800.dp, 400.dp)) {
                    ShellTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "shell_landscape_dark")
    }

    @Test
    fun settingsPortraitLight() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SettingsTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "settings_portrait_light")
    }

    @Test
    fun settingsPortraitDark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SettingsTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "settings_portrait_dark")
    }
}
