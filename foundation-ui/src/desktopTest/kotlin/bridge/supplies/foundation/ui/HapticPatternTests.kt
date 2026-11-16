package bridge.supplies.foundation.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class HapticPatternTests {
    private class Recorder : HapticFeedback {
        val performed = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performed += hapticFeedbackType
        }
    }

    private val config =
        FoundationConfig(
            appName = "Haptic Tests",
            packageName = "bridge.supplies.foundation.ui.test",
            version = "2.2.0",
            build = "1",
            encryptionKey = "1234567890123456",
        )

    private fun viewModel(
        vibration: Boolean,
        features: Set<PlatformFeature> = setOf(PlatformFeature.VIBRATION),
    ): FoundationViewModel {
        val model =
            FoundationViewModel(
                config,
                FakeFoundationPlatform(features = features),
                InMemoryFoundationSettings(mapOf(FoundationSettingKeys.vibration to vibration)),
            )
        // Wait for the stored value, as the app does by the time the user taps.
        runBlocking {
            withTimeout(5_000) {
                while (model.setting(FoundationSettingKeys.vibration).value != vibration) {
                    kotlinx.coroutines.yield()
                }
            }
        }
        return model
    }

    @Test
    fun patternsMapToDistinctTypes() {
        val types = HapticPattern.entries.map { it.toHapticFeedbackType() }
        assertEquals(HapticPattern.entries.size, types.toSet().size)
        assertEquals(HapticFeedbackType.TextHandleMove, HapticPattern.Click.toHapticFeedbackType())
    }

    @Test
    fun everyPatternVibratesWhenOn() {
        val model = viewModel(vibration = true)
        val recorder = Recorder()
        HapticPattern.entries.forEach { model.hapticFeedback(recorder, it) }
        assertEquals(HapticPattern.entries.map { it.toHapticFeedbackType() }, recorder.performed)
    }

    @Test
    fun noPatternVibratesWhenSettingIsOff() {
        val model = viewModel(vibration = false)
        val recorder = Recorder()
        HapticPattern.entries.forEach { model.hapticFeedback(recorder, it) }
        model.hapticFeedback(recorder)
        assertTrue(recorder.performed.isEmpty())
    }

    @Test
    fun noPatternVibratesWithoutDeviceSupport() {
        val model = viewModel(vibration = true, features = emptySet())
        val recorder = Recorder()
        HapticPattern.entries.forEach { model.hapticFeedback(recorder, it) }
        assertTrue(recorder.performed.isEmpty())
    }

    @Test
    fun basicCallIsUnchanged() {
        val model = viewModel(vibration = true)
        val recorder = Recorder()
        model.hapticFeedback(recorder)
        assertEquals(listOf(HapticFeedbackType.TextHandleMove), recorder.performed)
    }
}
