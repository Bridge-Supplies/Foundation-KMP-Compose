package bridge.supplies.foundation.ui.components

import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import bridge.supplies.foundation.ui.shell.TRANSITION_EASING
import kotlin.math.max
import kotlinx.coroutines.delay

// Adapted for multiplatform from: https://dev.to/bmonjoie/jetpack-compose-reveal-effect-1fao

@Composable
public fun CircularReveal(
    modifier: Modifier = Modifier,
    startDelayMs: Int,
    revealDurationMs: Int,
    onCompleted: (suspend () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var isRevealed by remember { mutableStateOf(false) }

    val animationProgress: State<Float> =
        animateFloatAsState(
            targetValue = if (isRevealed) 1f else 0f,
            animationSpec = tween(revealDurationMs, easing = TRANSITION_EASING),
            label = "circular_reveal",
        )

    LaunchedEffect(Unit) {
        delay(startDelayMs.toLong())
        isRevealed = true
        delay(revealDurationMs.toLong())
        onCompleted?.invoke()
    }

    Box(modifier = modifier.circularReveal(animationProgress.value)) {
        content()
    }
}

public fun Modifier.circularReveal(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    offset: Offset? = null,
): Modifier = clip(CircularRevealShape(progress, offset))

private class CircularRevealShape(
    @param:FloatRange(from = 0.0, to = 1.0) private val progress: Float,
    private val offset: Offset? = null,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Generic(
            Path().apply {
                val startingOffset = offset ?: Offset(size.width / 2f, size.height / 2f)
                // end result slightly larger than full screen
                val revealRadius = max(size.width, size.height) * progress * 0.7f

                addOval(
                    Rect(
                        topLeft =
                            Offset(
                                x = startingOffset.x - revealRadius,
                                y = startingOffset.y - revealRadius,
                            ),
                        bottomRight =
                            Offset(
                                x = startingOffset.x + revealRadius,
                                y = startingOffset.y + revealRadius,
                            ),
                    )
                )
            }
        )
    }
}
