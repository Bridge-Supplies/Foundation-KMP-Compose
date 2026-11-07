package bridge.supplies.foundation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.theme_settings_title
import bridge.supplies.foundation.ui.shell.BackButton
import bridge.supplies.foundation.ui.shell.Chrome
import bridge.supplies.foundation.ui.shell.FoundationApp
import bridge.supplies.foundation.ui.shell.LocalNavigator
import bridge.supplies.foundation.ui.shell.Navigator
import bridge.supplies.foundation.ui.shell.ScreenBackHandler
import bridge.supplies.foundation.ui.shell.TopBarAction
import bridge.supplies.foundation.ui.shell.TopBarActions
import bridge.supplies.foundation.ui.shell.screenMetadata
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@OptIn(ExperimentalTestApi::class)
class ShellUiTests {

    private lateinit var settings: InMemoryFoundationSettings

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

    /** Whether the editor screen keeps back for itself, and how often it did. */
    private var editorKeepsBack by mutableStateOf(true)
    private var editorBackCount = 0

    @Composable
    private fun TestAppShell(onNavigatorReady: (Navigator) -> Unit = {}) {
        CompositionLocalProvider(
            LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(800, 1200))
        ) {
            Box(Modifier.size(400.dp, 600.dp)) {
                FoundationApp(
                    tabs = testTabs,
                    routes = testRouteRegistry,
                ) {
                    entry<Tab1RootRoute>(metadata = screenMetadata(titleText = "Tab 1 Root")) {
                        val nav = LocalNavigator.current
                        onNavigatorReady(nav)
                        TopBarActions(
                            listOf(
                                TopBarAction(
                                    icon = Icons.Default.Info,
                                    label = Res.string.theme_settings_title,
                                    onClick = {},
                                )
                            )
                        )
                        Column {
                            Text("Tab 1 Content")
                            Button(onClick = { nav.navigate(Tab1DetailRoute) }) {
                                Text("Go To Detail")
                            }
                            Button(onClick = { nav.navigate(Tab1EditorRoute) }) {
                                Text("Go To Editor")
                            }
                        }
                    }

                    entry<Tab1DetailRoute>(metadata = screenMetadata(titleText = "Tab 1 Detail")) {
                        val nav = LocalNavigator.current
                        onNavigatorReady(nav)
                        Column {
                            Text("Tab 1 Detail Screen")
                            Button(onClick = { nav.back() }) {
                                Text("Detail Back Button")
                            }
                        }
                    }

                    entry<Tab1EditorRoute>(
                        metadata =
                            screenMetadata(
                                titleText = "Editor",
                                chrome = Chrome.TopBarOnly,
                                backButton = BackButton.Close,
                            )
                    ) {
                        ScreenBackHandler(enabled = editorKeepsBack) { editorBackCount++ }
                        Text("Editor Content")
                    }

                    entry<Tab2RootRoute>(
                        metadata = screenMetadata(titleText = "Tab 2 Custom Title")
                    ) {
                        val nav = LocalNavigator.current
                        onNavigatorReady(nav)
                        Text("Tab 2 Content")
                    }

                    entry<Tab3ImmersiveRoute>(
                        metadata =
                            screenMetadata(
                                titleText = "Immersive Title",
                                chrome = Chrome.Immersive(),
                            )
                    ) {
                        val nav = LocalNavigator.current
                        onNavigatorReady(nav)
                        Text("Immersive Content")
                    }
                }
            }
        }
    }

    @Test
    fun tabsSwitchAndKeepTheirStacks() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        onNodeWithText("Tab 1 Content").assertIsDisplayed()

        // Navigate to detail on Tab 1
        onNodeWithText("Go To Detail").performClick()
        onNodeWithText("Tab 1 Detail Screen").assertIsDisplayed()

        // Switch to Tab 2
        runOnUiThread {
            navigator!!.selectTab("tab2")
        }
        onNodeWithText("Tab 2 Content").assertIsDisplayed()

        // Switch back to Tab 1: stack is kept so Detail Screen should still be displayed
        runOnUiThread {
            navigator!!.selectTab("tab1")
        }
        onNodeWithText("Tab 1 Detail Screen").assertIsDisplayed()
    }

    @Test
    fun reselectingCurrentTabPopsToRoot() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        // Navigate to detail
        onNodeWithText("Go To Detail").performClick()
        onNodeWithText("Tab 1 Detail Screen").assertIsDisplayed()

        // Reselect current tab (tab1)
        runOnUiThread {
            navigator!!.selectTab("tab1")
        }

        // Should pop to root
        onNodeWithText("Tab 1 Content").assertIsDisplayed()
    }

    @Test
    fun backFromTabRootGoesToFirstTab() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        // Switch to Tab 2
        runOnUiThread {
            navigator!!.selectTab("tab2")
        }
        onNodeWithText("Tab 2 Content").assertIsDisplayed()
        assertEquals("tab2", navigator!!.currentTabId)

        // Trigger back from Tab 2 root
        var handled = false
        runOnUiThread {
            handled = navigator!!.back()
        }

        assertTrue(handled)
        assertEquals("tab1", navigator!!.currentTabId)
        onNodeWithText("Tab 1 Content").assertIsDisplayed()

        // Back from Tab 1 root should return false (app close)
        runOnUiThread {
            handled = navigator!!.back()
        }
        assertFalse(handled)
    }

    @Test
    fun screenMetadataTitlesAppearInTopBar() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        // titleText metadata on Tab 1 Root
        onNodeWithText("Tab 1 Root").assertIsDisplayed()

        // titleText metadata on Tab 2
        runOnUiThread {
            navigator!!.selectTab("tab2")
        }
        onNodeWithText("Tab 2 Custom Title").assertIsDisplayed()
    }

    @Test
    fun topBarActionsShowForCurrentScreenOnly() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        // TopBarAction with icon and Res.string.theme_settings_title ("Theming")
        onNodeWithContentDescription("Theming").assertIsDisplayed()

        // Navigate to detail: actions should not show
        onNodeWithText("Go To Detail").performClick()
        onNodeWithText("Tab 1 Detail Screen").assertIsDisplayed()
        onNodeWithContentDescription("Theming").assertDoesNotExist()

        // Go back: action should reappear
        onNodeWithText("Detail Back Button").performClick()
        onNodeWithText("Tab 1 Content").assertIsDisplayed()
        onNodeWithContentDescription("Theming").assertIsDisplayed()
    }

    @Test
    fun chromeImmersiveHidesBars() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent {
            TestAppShell { navigator = it }
        }

        // Standard screen shows top bar title
        onNodeWithText("Tab 1 Root").assertIsDisplayed()

        // Navigate to Immersive tab
        runOnUiThread {
            navigator!!.selectTab("tab3")
        }
        onNodeWithText("Immersive Content").assertIsDisplayed()

        // In Chrome.Immersive, top bar is hidden (title should not be displayed)
        onNodeWithText("Immersive Title").assertDoesNotExist()
    }

    @Test
    fun topBarOnly_showsTheTitleAndClose_withoutTheTabs() = runComposeUiTest {
        setContent { TestAppShell() }
        onNodeWithContentDescription("System", useUnmergedTree = true).assertIsDisplayed()

        onNodeWithText("Go To Editor").performClick()
        onNodeWithText("Editor Content").assertIsDisplayed()
        onNodeWithText("Editor").assertIsDisplayed()
        onNodeWithContentDescription("Close").assertIsDisplayed()
        onNodeWithContentDescription("Back").assertDoesNotExist()
        onNodeWithContentDescription("System", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun aScreenBackHandler_getsTheTopBarsBack_untilItLetsGo() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent { TestAppShell { navigator = it } }
        onNodeWithText("Go To Editor").performClick()

        onNodeWithContentDescription("Close").performClick()
        onNodeWithText("Editor Content").assertIsDisplayed()
        assertEquals(1, editorBackCount)

        assertEquals(Tab1EditorRoute, navigator!!.currentRoute)

        runOnUiThread { editorKeepsBack = false }
        waitForIdle()
        onNodeWithContentDescription("Close").performClick()
        onNodeWithText("Tab 1 Content").assertIsDisplayed()
        assertEquals(1, editorBackCount)
    }

    @Test
    fun screensArePaddedForTheirOwnBars() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent { TestAppShell { navigator = it } }

        // Below the top bar, and clear of the bottom bar
        val standard = onNodeWithText("Tab 1 Content").getBoundsInRoot()
        assertTrue(standard.top >= 64.dp, "below the top bar: ${standard.top}")

        onNodeWithText("Go To Editor").performClick()
        onNodeWithText("Editor Content").assertIsDisplayed()
        val editor = onNodeWithText("Editor Content").getBoundsInRoot()
        assertTrue(editor.top >= 64.dp, "below the top bar: ${editor.top}")

        runOnUiThread { navigator!!.selectTab("tab3") }
        onNodeWithText("Immersive Content").assertIsDisplayed()
        assertEquals(0.dp, onNodeWithText("Immersive Content").getBoundsInRoot().top)
    }

    @Test
    fun pushingAndPopping_animateBetweenBothScreens() = runComposeUiTest {
        setContent { TestAppShell() }
        waitForIdle()
        mainClock.autoAdvance = false

        // Partway through, both screens are composed
        onNodeWithText("Go To Detail").performClick()
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(100)
        assertTrue(onAllNodesWithText("Tab 1 Content").fetchSemanticsNodes().isNotEmpty())
        assertTrue(onAllNodesWithText("Tab 1 Detail Screen").fetchSemanticsNodes().isNotEmpty())

        mainClock.advanceTimeBy(1_000)
        onNodeWithText("Detail Back Button").performClick()
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(100)
        assertTrue(onAllNodesWithText("Tab 1 Content").fetchSemanticsNodes().isNotEmpty())
        assertTrue(onAllNodesWithText("Tab 1 Detail Screen").fetchSemanticsNodes().isNotEmpty())
    }
}
