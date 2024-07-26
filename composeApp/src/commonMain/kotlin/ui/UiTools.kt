package ui

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.action_settings
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import kotlin.math.max

enum class AppBarAction(
    val labelRes: StringResource,
    val icon: ImageVector
) {
    SETTINGS(
        Res.string.action_settings,
        Icons.Default.Settings
    )
}


// Adapted for multiplatform from: https://dev.to/bmonjoie/jetpack-compose-reveal-effect-1fao

@Composable
fun CircularReveal(
    modifier: Modifier = Modifier,
    startDelayMs: Int = TRANSITION_ENTER_MS,
    revealDurationMs: Int = 1000,
    onCompleted: (suspend () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    
    val animationProgress: State<Float> = animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = revealDurationMs, easing = TRANSITION_EASING),
        label = "circular_reveal"
    )
    
    LaunchedEffect(Unit) {
        delay(startDelayMs.toLong())
        isRevealed = true
        delay(revealDurationMs.toLong())
        onCompleted?.invoke()
    }
    
    Box(
        modifier = modifier
            .circularReveal(animationProgress.value)
    ) {
        content()
    }
}

fun Modifier.circularReveal(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    offset: Offset? = null
) = clip(CircularRevealShape(progress, offset))

private class CircularRevealShape(
    @FloatRange(from = 0.0, to = 1.0) private val progress: Float,
    private val offset: Offset? = null
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            Path().apply {
                val startingOffset = offset ?: Offset(size.width / 2f, size.height / 2f)
                // end result slightly larger than full screen
                val revealRadius = max(size.width, size.height) * progress * 0.7f
                
                addOval(
                    Rect(
                        topLeft = Offset(
                            x = startingOffset.x - revealRadius,
                            y = startingOffset.y - revealRadius
                        ),
                        bottomRight = Offset(
                            x = startingOffset.x + revealRadius,
                            y = startingOffset.y + revealRadius
                        )
                    )
                )
            }
        )
    }
}


// Scrollable edge fade lists

@Composable
fun EdgeFadeGrid(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    gridState: LazyGridState,
    fadeSize: Dp = 24.dp,
    gridContent: @Composable () -> Unit
) {
    val orientation = gridState.layoutInfo.orientation
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val lastVisibleItemInfo = layoutInfo.visibleItemsInfo.last()
                val lastItemBottom = lastVisibleItemInfo.offset.y + lastVisibleItemInfo.size.height
                // Check if the last item's end edge is below the viewport's end (indicating it's not fully visible)
                lastItemBottom > layoutInfo.viewportEndOffset
            } else {
                false
            }
        }
    }
    
    Box(
        modifier = modifier
    ) {
        gridContent()
        
        AnimatedEdgeFade(
            colorScheme = colorScheme,
            isVisible = showStartEdgeFade,
            orientation = orientation,
            isStartEdge = true,
            fadeSize = fadeSize
        )
        
        AnimatedEdgeFade(
            colorScheme = colorScheme,
            isVisible = showEndEdgeFade,
            orientation = orientation,
            isStartEdge = false,
            fadeSize = fadeSize
        )
    }
}


@Composable
fun EdgeFadeList(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    listState: LazyListState,
    fadeSize: Dp = 24.dp,
    listContent: @Composable () -> Unit
) {
    val orientation = listState.layoutInfo.orientation
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastVisibleItem ->
                val isLastItemFullyVisible = lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
                // The last visible item is not the last item in the list or it's not fully visible
                (lastVisibleItem.index + 1 < layoutInfo.totalItemsCount) || !isLastItemFullyVisible
            } ?: false
        }
    }
    
    Box(
        modifier = modifier
    ) {
        listContent()
        
        AnimatedEdgeFade(
            colorScheme = colorScheme,
            isVisible = showStartEdgeFade,
            orientation = orientation,
            isStartEdge = true,
            fadeSize = fadeSize
        )
        
        AnimatedEdgeFade(
            colorScheme = colorScheme,
            isVisible = showEndEdgeFade,
            orientation = orientation,
            isStartEdge = false,
            fadeSize = fadeSize
        )
    }
}


@Composable
fun BoxScope.AnimatedEdgeFade(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    isVisible: Boolean,
    orientation: Orientation,
    isStartEdge: Boolean,
    fadeSize: Dp
) {
    val alignment by remember(orientation, isStartEdge) {
        derivedStateOf {
            when {
                orientation == Orientation.Horizontal && isStartEdge ->
                    Alignment.CenterStart
                
                orientation == Orientation.Horizontal && !isStartEdge ->
                    Alignment.CenterEnd
                
                orientation == Orientation.Vertical && isStartEdge ->
                    Alignment.TopCenter
                
                else ->
                    Alignment.BottomCenter
            }
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(alignment)
            .then(
                when (orientation) {
                    Orientation.Horizontal -> Modifier.width(fadeSize)
                    Orientation.Vertical -> Modifier.height(fadeSize)
                }
            )
    ) {
        val colors = listOf(
            if (isStartEdge) colorScheme.background else Color.Transparent,
            if (isStartEdge) Color.Transparent else colorScheme.background
        )
        
        Box(
            modifier = Modifier
                .background(
                    when (orientation) {
                        Orientation.Horizontal ->
                            Brush.horizontalGradient(colors = colors)
                        
                        Orientation.Vertical ->
                            Brush.verticalGradient(colors = colors)
                    }
                )
                .then(
                    when (orientation) {
                        Orientation.Horizontal ->
                            Modifier.fillMaxHeight()
                        
                        Orientation.Vertical ->
                            Modifier.fillMaxWidth()
                    }
                )
        )
    }
}