package bridge.supplies.foundation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.ui.components.CodeDisplay
import bridge.supplies.foundation.ui.components.HintText
import bridge.supplies.foundation.ui.components.TextButton
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.share_sheet_copy_text
import bridge.supplies.foundation.ui.settings.BuiltInSettings
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.settings.FoundationSettingsScreen
import bridge.supplies.foundation.ui.sheets.CloseButton
import bridge.supplies.foundation.ui.sheets.SheetColumn
import bridge.supplies.foundation.ui.sheets.SheetScope
import bridge.supplies.foundation.ui.shell.BackButton
import bridge.supplies.foundation.ui.shell.Chrome
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.LocalRoute
import bridge.supplies.foundation.ui.shell.LocalTopBarActionHost
import bridge.supplies.foundation.ui.shell.TopBarActionHost
import bridge.supplies.foundation.ui.shell.screenMetadata
import bridge.supplies.foundation.ui.theme.ColorTheme
import bridge.supplies.foundation.ui.theme.DarkMode
import bridge.supplies.foundation.ui.theme.ExpressiveAnimation
import bridge.supplies.foundation.ui.theme.FoundationTheme
import bridge.supplies.foundation.ui.theme.Palette
import bridge.supplies.foundation.ui.theme.ThemeConfig
import kotlin.test.AfterTest
import kotlin.test.Test
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

private data object DummySettingsScreenshotRoute : NavKey

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class ScreenshotGoldensTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private fun setupTestKoin(darkMode: DarkMode) {
        stopKoin()
        val settings =
            InMemoryFoundationSettings(
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
            routes = testRouteRegistry,
        ) {
            entry<Tab1RootRoute>(metadata = screenMetadata(titleText = "Home")) {
                Column {
                    Text("Home Content")
                }
            }
            entry<Tab2RootRoute>(metadata = screenMetadata(titleText = "Settings")) {
                Text("Settings Content")
            }
        }
    }

    @Composable
    private fun TopBarOnlyTestContent() {
        FoundationApp(
            tabs = testTabs,
            routes = testRouteRegistry,
        ) {
            entry<Tab1RootRoute>(
                metadata =
                    screenMetadata(
                        titleText = "Editor",
                        chrome = Chrome.TopBarOnly,
                        backButton = BackButton.Close,
                    )
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Editor Content")
                }
            }
            entry<Tab2RootRoute>(metadata = screenMetadata(titleText = "Settings")) {
                Text("Settings Content")
            }
        }
    }

    @Composable
    private fun SheetTestContent() {
        val dummyScope = remember {
            object : SheetScope {
                override fun dismiss() {}

                override fun hapticFeedback() {}
            }
        }
        FoundationTheme(ThemeConfig(expressiveAnimation = ExpressiveAnimation.USER_CHOICE)) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BottomSheetDefaults.DragHandle()
                        SheetColumn {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CodeDisplay(
                                    modifier = Modifier.wrapContentWidth().height(200.dp),
                                    text = "https://example.com/share",
                                    color = MaterialTheme.colorScheme.primary,
                                    backgroundColor = MaterialTheme.colorScheme.onPrimary,
                                    cardColor = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            HintText(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                text = "https://example.com/share",
                            )
                            TextButton(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                text = stringResource(Res.string.share_sheet_copy_text),
                            ) {}
                            dummyScope.CloseButton()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SettingsTestContent() {
        val actionHost = remember { TopBarActionHost() }
        CompositionLocalProvider(
            LocalRoute provides DummySettingsScreenshotRoute,
            LocalTopBarActionHost provides actionHost,
        ) {
            FoundationTheme(ThemeConfig(expressiveAnimation = ExpressiveAnimation.USER_CHOICE)) {
                FoundationSettingsScreen(
                    builtIns =
                        BuiltInSettings(
                            theme = true,
                            encryptedSharing = true,
                            landingTips = true,
                            expressiveAnimation = true,
                            fullscreenLandscape = true,
                            vibration = true,
                            scrollHelpers = true,
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

    @Test
    fun topBarOnlyLight() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    TopBarOnlyTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "top_bar_only_light")
    }

    @Test
    fun topBarOnlyDark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    TopBarOnlyTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "top_bar_only_dark")
    }

    @Test
    fun sheetLight() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SheetTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "sheet_light")
    }

    @Test
    fun sheetDark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SheetTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "sheet_dark")
    }

    @Test
    fun settingsFontScale15Light() = runComposeUiTest {
        setupTestKoin(DarkMode.LIGHT)
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.5f),
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800)),
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SettingsTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "settings_font_scale_1_5_light")
    }

    @Test
    fun settingsFontScale15Dark() = runComposeUiTest {
        setupTestKoin(DarkMode.DARK)
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.5f),
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(400, 800)),
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    SettingsTestContent()
                }
            }
        }
        val image = onRoot().captureToImage().toAwtImage()
        GoldenTestHelper.assertGolden(image, "settings_font_scale_1_5_dark")
    }
}
