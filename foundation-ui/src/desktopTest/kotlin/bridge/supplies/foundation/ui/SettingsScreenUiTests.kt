package bridge.supplies.foundation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import bridge.supplies.foundation.core.settings.BooleanSetting
import bridge.supplies.foundation.core.settings.settingName
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.invalid_date
import bridge.supplies.foundation.ui.resources.share_sheet_copy_text
import bridge.supplies.foundation.ui.settings.BuiltInSettings
import bridge.supplies.foundation.ui.settings.FoundationSettingsScreen
import bridge.supplies.foundation.ui.shell.LocalRoute
import bridge.supplies.foundation.ui.shell.LocalTopBarActionHost
import bridge.supplies.foundation.ui.shell.TopBarActionHost
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private data object DummySettingsRoute : NavKey

@OptIn(ExperimentalTestApi::class)
class SettingsScreenUiTests {

    private lateinit var settings: InMemoryFoundationSettings
    private val customSwitchKey = BooleanSetting(settingName("custom_test_feature"), default = false)

    @BeforeTest
    fun setUp() {
        stopKoin()
        settings = InMemoryFoundationSettings()
        startKoin {
            modules(testFoundationModule(settings))
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun settingsScreenRendersBuiltInsAndCustomSwitchWritesThroughFakeSettings() = runComposeUiTest {
        runBlocking {
            assertFalse(settings.get(customSwitchKey))
        }

        setContent {
            val actionHost = remember { TopBarActionHost() }
            CompositionLocalProvider(
                LocalRoute provides DummySettingsRoute,
                LocalTopBarActionHost provides actionHost,
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(800, 1200))
            ) {
                Box(Modifier.size(400.dp, 800.dp)) {
                    FoundationSettingsScreen(
                        builtIns = BuiltInSettings(
                            theme = true,
                            encryptedSharing = false,
                            landingTips = false,
                            expressiveAnimation = false,
                            fullscreenLandscape = false,
                            vibration = false,
                            scrollHelpers = false
                        ),
                        extra = {
                            section(Res.string.invalid_date) {
                                switch(
                                    key = customSwitchKey,
                                    title = Res.string.share_sheet_copy_text
                                )
                            }
                        }
                    )
                }
            }
        }

        // Built-ins: "Dark mode" item should be visible
        onNodeWithText("Dark mode").assertIsDisplayed()

        // Custom section switch title
        val switchNode = onNodeWithText("Copy to clipboard")
        switchNode.assertIsDisplayed()

        // Toggle the switch
        switchNode.performClick()

        // Check that fake settings received the write
        waitUntil(timeoutMillis = 2000) {
            runBlocking { settings.get(customSwitchKey) }
        }

        runBlocking {
            assertTrue(settings.get(customSwitchKey))
        }
    }
}
