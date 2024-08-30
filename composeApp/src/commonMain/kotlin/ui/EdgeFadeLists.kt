package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun EdgeFadeBase(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    showStartEdgeFade: Boolean,
    showEndEdgeFade: Boolean,
    orientation: Orientation,
    fadeSize: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        content()
        
        AnimatedEdgeFade(
            color = color,
            isVisible = showStartEdgeFade,
            orientation = orientation,
            isStartEdge = true,
            fadeSize = fadeSize
        )
        
        AnimatedEdgeFade(
            color = color,
            isVisible = showEndEdgeFade,
            orientation = orientation,
            isStartEdge = false,
            fadeSize = fadeSize
        )
    }
}

@Composable
fun EdgeFadeColumn(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    scrollState: ScrollState,
    fadeSize: Dp = 16.dp,
    columnContent: @Composable () -> Unit
) {
    val showStartEdgeFade by remember {
        derivedStateOf {
            scrollState.value > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            scrollState.value < scrollState.maxValue
        }
    }
    
    EdgeFadeBase(
        modifier = modifier,
        color = color,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeSize = fadeSize,
        content = columnContent
    )
}

@Composable
fun EdgeFadeGrid(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    gridState: LazyGridState,
    fadeSize: Dp = 16.dp,
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
    
    EdgeFadeBase(
        modifier = modifier,
        color = color,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = orientation,
        fadeSize = fadeSize,
        content = gridContent
    )
}

@Composable
fun EdgeFadeStaggeredGrid(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    gridState: LazyStaggeredGridState,
    fadeSize: Dp = 16.dp,
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
    
    EdgeFadeBase(
        modifier = modifier,
        color = color,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = orientation,
        fadeSize = fadeSize,
        content = gridContent
    )
}

@Composable
fun EdgeFadeLazyList(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    listState: LazyListState,
    fadeSize: Dp = 16.dp,
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
    
    EdgeFadeBase(
        modifier = modifier,
        color = color,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = orientation,
        fadeSize = fadeSize,
        content = listContent
    )
}

@Composable
fun EdgeFadeIndexedLazyList(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    indexedListState: IndexedLazyListState,
    fadeSize: Dp = 16.dp,
    listContent: @Composable () -> Unit
) {
    val listState = indexedListState.listState
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
    
    EdgeFadeBase(
        modifier = modifier,
        color = color,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = orientation,
        fadeSize = fadeSize,
        content = listContent
    )
}

@Composable
fun BoxScope.AnimatedEdgeFade(
    color: Color = MaterialTheme.colorScheme.background,
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
            if (isStartEdge) color else Color.Transparent,
            if (isStartEdge) Color.Transparent else color
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