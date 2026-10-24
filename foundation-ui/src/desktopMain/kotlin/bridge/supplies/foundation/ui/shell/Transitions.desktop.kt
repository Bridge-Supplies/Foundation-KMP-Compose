package bridge.supplies.foundation.ui.shell

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable

@Composable
public actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // no-op
}

public actual val TRANSITION_ENTER_MS: Int = 200
public actual val TRANSITION_EXIT_MS: Int = 100
public actual val TRANSITION_EASING: Easing = FastOutLinearInEasing
public actual val TRANSITION_OFFSET_DIV: Int = 10

internal actual val RAIL_VERTICALLY_CENTERED = false
internal actual val CENTER_TOP_BAR_TITLE = false

public actual fun ScreenEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + scaleIn(
        initialScale = 1f - 1f / TRANSITION_OFFSET_DIV,
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + scaleOut(
        targetScale = 1f + 1f / TRANSITION_OFFSET_DIV,
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenPopEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + scaleIn(
        initialScale = 1f + 1f / TRANSITION_OFFSET_DIV,
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

public actual fun ScreenPopExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + scaleOut(
        targetScale = 1f - 1f / TRANSITION_OFFSET_DIV,
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )