package screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

actual val TRANSITION_ENTER_MS = 300
actual val TRANSITION_EXIT_MS = 200
actual val TRANSITION_EASING = FastOutSlowInEasing
actual val TRANSITION_OFFSET_DIV = 6

actual fun ScreenEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + slideInHorizontally(
        initialOffsetX = { it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

actual fun ScreenExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )

actual fun ScreenPopEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    ) + slideInHorizontally(
        initialOffsetX = { -it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )

actual fun ScreenPopExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    ) + slideOutHorizontally(
        targetOffsetX = { it / TRANSITION_OFFSET_DIV },
        animationSpec = tween(TRANSITION_EXIT_MS, easing = TRANSITION_EASING)
    )