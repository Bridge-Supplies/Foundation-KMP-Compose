package bridge.supplies.foundation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
 * Foundation's shared state: settings (Foundation's and the app's), platform and config. App state
 * belongs in the app's own ViewModels, which can inject [FoundationSettings] directly.
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

    /**
     * Vibrates with [pattern] under the same conditions as [hapticFeedback]: the device supports it
     * and the user hasn't turned haptic feedback off.
     */
    public fun hapticFeedback(haptics: HapticFeedback, pattern: HapticPattern) {
        if (supports(PlatformFeature.VIBRATION) && setting(FoundationSettingKeys.vibration).value) {
            haptics.performHapticFeedback(pattern.toHapticFeedbackType())
        }
    }
}

/**
 * Named vibration patterns for [rememberHapticPatterns]. All of them follow the user's "Haptic
 * feedback" setting.
 */
public enum class HapticPattern {
    /** A light tap, the same as [rememberHapticFeedback]. For ordinary taps. */
    Click,
    /** A short success pulse. For a confirmed action. */
    Confirm,
    /** A strong, long pulse. For a big moment such as a hit, a defeat or a result. */
    Heavy,
    /** A short failure buzz. For a refused or invalid action. */
    Reject,
}

internal fun HapticPattern.toHapticFeedbackType(): HapticFeedbackType =
    when (this) {
        HapticPattern.Click -> HapticFeedbackType.TextHandleMove
        HapticPattern.Confirm -> HapticFeedbackType.Confirm
        HapticPattern.Heavy -> HapticFeedbackType.LongPress
        HapticPattern.Reject -> HapticFeedbackType.Reject
    }

/** Koin module for foundation-ui. Include it next to `foundationCoreModule(config)`. */
public fun foundationUiModule(): Module = module {
    single { FoundationViewModel(get(), get(), get()) }
}

@Composable public fun foundationViewModel(): FoundationViewModel = koinInject()

/**
 * Haptic feedback honoring the user's setting, e.g. `val haptic = rememberHapticFeedback(); onClick
 * = { haptic() }`.
 */
@Composable
public fun rememberHapticFeedback(): () -> Unit {
    val viewModel = foundationViewModel()
    val haptics = LocalHapticFeedback.current
    return remember(viewModel, haptics) { { viewModel.hapticFeedback(haptics) } }
}

/**
 * Haptic feedback with named [HapticPattern]s, honoring the user's setting, e.g. `val haptic =
 * rememberHapticPatterns(); onClick = { haptic(HapticPattern.Confirm) }`.
 */
@Composable
public fun rememberHapticPatterns(): (HapticPattern) -> Unit {
    val viewModel = foundationViewModel()
    val haptics = LocalHapticFeedback.current
    return remember(viewModel, haptics) {
        // Start loading the setting now, so the first tap already knows its stored value.
        viewModel.setting(FoundationSettingKeys.vibration)
        val perform: (HapticPattern) -> Unit = { viewModel.hapticFeedback(haptics, it) }
        perform
    }
}

/** Whether the window is taller than it is wide. */
@Composable
public fun isPortrait(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return size.width <= size.height
}

/** A window at least this long on its short side is a tablet; anything smaller is a phone. */
public val TabletMinSize: Dp = 600.dp

/**
 * Whether the window is tablet-sized. With [isPortrait] this gives four layouts: phone portrait,
 * phone landscape, tablet portrait and tablet landscape.
 */
@Composable
public fun isTablet(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    val shortSide = with(LocalDensity.current) { minOf(size.width, size.height).toDp() }
    return shortSide >= TabletMinSize
}
