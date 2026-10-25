package bridge.supplies.foundation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import bridge.supplies.foundation.ui.components.TextInput
import bridge.supplies.foundation.ui.theme.FoundationTheme
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TextInputUiTests {

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
}
