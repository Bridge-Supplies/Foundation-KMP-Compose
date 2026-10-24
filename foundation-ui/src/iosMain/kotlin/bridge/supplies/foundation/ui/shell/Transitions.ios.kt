package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable

@Composable
public actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // no-op
}

internal actual val TRANSITION_ENTER_MS: Int = 450
internal actual val TRANSITION_EXIT_MS: Int = 300
internal actual val TRANSITION_EASING: Easing = LinearOutSlowInEasing
internal actual val TRANSITION_OFFSET_DIV: Int = 4

internal actual val RAIL_VERTICALLY_CENTERED = true
internal actual val CENTER_TOP_BAR_TITLE = true

internal actual fun ScreenEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

internal actual fun ScreenExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )

internal actual fun ScreenPopEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + slideInHorizontally(
        initialOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

internal actual fun ScreenPopExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )