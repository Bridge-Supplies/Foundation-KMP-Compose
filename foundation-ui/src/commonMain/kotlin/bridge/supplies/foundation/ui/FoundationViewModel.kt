package bridge.supplies.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.supplies.foundation.core.config.FoundationConfig
import bridge.supplies.foundation.core.platform.FoundationPlatform
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.core.settings.FoundationSettings
import bridge.supplies.foundation.core.settings.SettingKey
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Foundation's shared state: settings (Foundation's and the app's), platform and config.
 * App state belongs in the app's own ViewModels, which can inject [FoundationSettings] directly.
 */
public class FoundationViewModel(
    public val config: FoundationConfig,
    public val platform: FoundationPlatform,
    private val settings: FoundationSettings,
) : ViewModel() {

    // Default dispatcher: desktop has no Main dispatcher unless the app adds coroutines-swing
    private val scope = CoroutineScope(viewModelScope.coroutineContext + Dispatchers.Default)
    private val states = mutableMapOf<String, StateFlow<*>>()

    /** The current value of [key], starting at its default until the stored value loads. */
    @Suppress("UNCHECKED_CAST")
    public fun <T> setting(key: SettingKey<T>): StateFlow<T> =
        states.getOrPut(key.name) {
            settings.flow(key).stateIn(scope, SharingStarted.Eagerly, key.default)
        } as StateFlow<T>

    public fun <T> set(key: SettingKey<T>, value: T) {
        scope.launch { settings.set(key, value) }
    }

    /** Reads the stored value of [key], waiting for storage. */
    public suspend fun <T> get(key: SettingKey<T>): T = settings.get(key)

    public fun supports(feature: PlatformFeature): Boolean = platform.supports(feature)

    /** Vibrates when the device supports it and the user hasn't turned haptic feedback off. */
    public fun hapticFeedback(haptics: HapticFeedback) {
        if (supports(PlatformFeature.VIBRATION) && setting(FoundationSettingKeys.vibration).value) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

/** Koin module for foundation-ui. Include it next to `foundationCoreModule(config)`. */
public fun foundationUiModule(): Module = module {
    single { FoundationViewModel(get(), get(), get()) }
}

@Composable
public fun foundationViewModel(): FoundationViewModel = koinInject()

/** Haptic feedback honoring the user's setting, e.g. `val haptic = rememberHapticFeedback(); onClick = { haptic() }`. */
@Composable
public fun rememberHapticFeedback(): () -> Unit {
    val viewModel = foundationViewModel()
    val haptics = LocalHapticFeedback.current
    return remember(viewModel, haptics) { { viewModel.hapticFeedback(haptics) } }
}

/** Whether the window is taller than it is wide. */
@Composable
public fun isPortrait(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return size.width <= size.height
}
