package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable

// Platform-specific navigation transitions and back handling

@Composable
public expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit = { }
)

internal expect val TRANSITION_ENTER_MS: Int
internal expect val TRANSITION_EXIT_MS: Int
internal expect val TRANSITION_EASING: Easing
internal expect val TRANSITION_OFFSET_DIV: Int

// Platform-specific shell layout defaults (see ShellOptions)
internal expect val RAIL_VERTICALLY_CENTERED: Boolean
internal expect val CENTER_TOP_BAR_TITLE: Boolean

internal fun TopLevelEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = TRANSITION_ENTER_MS,
            easing = TRANSITION_EASING
        )
    )

internal fun TopLevelExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = TRANSITION_EXIT_MS,
            easing = TRANSITION_EASING
        )
    )

internal expect fun ScreenEnterTransition(): EnterTransition
internal expect fun ScreenExitTransition(): ExitTransition
internal expect fun ScreenPopEnterTransition(): EnterTransition
internal expect fun ScreenPopExitTransition(): ExitTransition
