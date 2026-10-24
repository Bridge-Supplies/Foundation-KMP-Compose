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
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit = { }
)

expect val TRANSITION_ENTER_MS: Int
expect val TRANSITION_EXIT_MS: Int
expect val TRANSITION_EASING: Easing
expect val TRANSITION_OFFSET_DIV: Int

fun TopLevelEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = TRANSITION_ENTER_MS,
            easing = TRANSITION_EASING
        )
    )

fun TopLevelExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = TRANSITION_EXIT_MS,
            easing = TRANSITION_EASING
        )
    )

expect fun ScreenEnterTransition(): EnterTransition
expect fun ScreenExitTransition(): ExitTransition
expect fun ScreenPopEnterTransition(): EnterTransition
expect fun ScreenPopExitTransition(): ExitTransition
