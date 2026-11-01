package bridge.supplies.foundation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import bridge.supplies.foundation.ui.components.TextInput
import bridge.supplies.foundation.ui.theme.FoundationTheme
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@OptIn(ExperimentalTestApi::class)
class TextInputUiTests {

    private var focusManager: FocusManager? = null

    @BeforeTest
    fun setUp() {
        stopKoin()
        startKoin {
            modules(testFoundationModule(InMemoryFoundationSettings()))
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private fun SemanticsNodeInteraction.editableText(): String =
        fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    @Test
    fun lateStoredValuesDontOverwriteTyping() = runComposeUiTest {
        // The caller stores each change asynchronously; the test decides when stored values arrive.
        var stored by mutableStateOf("")
        val saves = mutableListOf<String>()
        setContent {
            FoundationTheme {
                TextInput(text = stored, hintText = "Notes", onValueChange = { saves += it })
            }
        }
        val field = onNode(hasSetTextAction())
        field.performClick()

        field.performTextInput("a")
        field.performTextInput("b")
        runOnIdle { stored = "a" } // the first keystroke's save arrives after the second keystroke
        field.performTextInput("c")
        runOnIdle { stored = "ab" }
        waitForIdle()

        assertEquals("abc", field.editableText())
        assertEquals("abc", saves.last())
    }

    @Test
    fun outsideChangesApplyWhileNotFocused() = runComposeUiTest {
        var stored by mutableStateOf("first")
        val saves = mutableListOf<String>()
        setContent {
            FoundationTheme {
                TextInput(text = stored, hintText = "Notes", onValueChange = { saves += it })
            }
        }
        val field = onNode(hasSetTextAction())
        assertEquals("first", field.editableText())

        runOnIdle { stored = "second" }
        waitForIdle()

        assertEquals("second", field.editableText())
        assertEquals(emptyList(), saves) // showing the caller's own value doesn't save it back
    }

    @Test
    fun undoingAKeystrokeBeforeItsSaveArrivesStillSaves() = runComposeUiTest {
        var stored by mutableStateOf("")
        val saves = mutableListOf<String>()
        setContent {
            FoundationTheme {
                TextInput(text = stored, hintText = "Notes", onValueChange = { saves += it })
            }
        }
        val field = onNode(hasSetTextAction())
        field.performClick()

        field.performTextInput("a")
        field.performTextReplacement("") // back to what's stored, but "a" is still being saved
        waitForIdle()
        runOnIdle { stored = "a" }
        waitForIdle()

        assertEquals("", field.editableText())
        assertEquals(listOf("a", ""), saves)
    }

    @Test
    fun outsideChangesApplyWhileFocused() = runComposeUiTest {
        var stored by mutableStateOf("first record")
        val saves = mutableListOf<String>()
        setContent {
            FoundationTheme {
                TextInput(text = stored, hintText = "Notes", onValueChange = { saves += it })
            }
        }
        val field = onNode(hasSetTextAction())
        field.performClick()
        waitForIdle()

        runOnIdle {
            stored = "second record"
        } // a clear button, or the sheet moving to another record
        waitForIdle()

        assertEquals("second record", field.editableText())
        assertEquals(emptyList(), saves)
    }

    @Test
    fun lateStoredValuesDontOverwriteTextAfterFocusIsLost() = runComposeUiTest {
        var stored by mutableStateOf("")
        val saves = mutableListOf<String>()
        setContent {
            focusManager = LocalFocusManager.current
            FoundationTheme {
                TextInput(text = stored, hintText = "Notes", onValueChange = { saves += it })
            }
        }
        val field = onNode(hasSetTextAction())
        field.performClick()
        field.performTextInput("a")
        field.performTextInput("b")
        field.performTextInput("c")
        waitForIdle()

        runOnIdle { focusManager?.clearFocus() }
        waitForIdle()
        runOnIdle {
            stored = "ab"
        } // the save of an earlier keystroke arrives after the field lost focus
        waitForIdle()

        assertEquals("abc", field.editableText())
        assertEquals("abc", saves.last())
    }
}
