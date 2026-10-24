package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
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
    androidx.activity.compose.BackHandler(enabled, onBack)
}

public actual val TRANSITION_ENTER_MS: Int = 300
public actual val TRANSITION_EXIT_MS: Int = 200
public actual val TRANSITION_EASING: Easing = FastOutSlowInEasing
public actual val TRANSITION_OFFSET_DIV: Int = 6

internal actual val RAIL_VERTICALLY_CENTERED = true
internal actual val CENTER_TOP_BAR_TITLE = false

public actual fun ScreenEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + slideInHorizontally(
        initialOffsetX = { it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenPopEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + slideInHorizontally(
        initialOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenPopExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + slideOutHorizontally(
        targetOffsetX = { it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )