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

public expect val TRANSITION_ENTER_MS: Int
public expect val TRANSITION_EXIT_MS: Int
public expect val TRANSITION_EASING: Easing
public expect val TRANSITION_OFFSET_DIV: Int

// Platform-specific shell layout defaults (see ShellOptions)
internal expect val RAIL_VERTICALLY_CENTERED: Boolean
internal expect val CENTER_TOP_BAR_TITLE: Boolean

public fun TopLevelEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = TRANSITION_ENTER_MS,
            easing = TRANSITION_EASING
        )
    )

public fun TopLevelExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = TRANSITION_EXIT_MS,
            easing = TRANSITION_EASING
        )
    )

public expect fun ScreenEnterTransition(): EnterTransition
public expect fun ScreenExitTransition(): ExitTransition
public expect fun ScreenPopEnterTransition(): EnterTransition
public expect fun ScreenPopExitTransition(): ExitTransition
