package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.verticalScroll
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

// WRAPPER

@Composable
fun BoxScope.AnimatedEdgeFade(
    isVisible: Boolean,
    orientation: Orientation,
    isStartEdge: Boolean,
    fadeColor: Color = MaterialTheme.colorScheme.background,
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
            if (isStartEdge) fadeColor else Color.Transparent,
            if (isStartEdge) Color.Transparent else fadeColor
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

@Composable
fun EdgeFadeWrapper(
    orientation: Orientation,
    showStartEdgeFade: Boolean,
    showEndEdgeFade: Boolean,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    wrappedContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        wrappedContent()
        
        AnimatedEdgeFade(
            isVisible = showStartEdgeFade,
            orientation = orientation,
            isStartEdge = true,
            fadeColor = fadeColor,
            fadeSize = fadeSize
        )
        
        AnimatedEdgeFade(
            isVisible = showEndEdgeFade,
            orientation = orientation,
            isStartEdge = false,
            fadeColor = fadeColor,
            fadeSize = fadeSize
        )
    }
}


// STATIC LISTS

@Composable
fun EdgeFadeColumn(
    modifier: Modifier = Modifier,
    state: ScrollState,
    verticalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.value > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            state.value < state.maxValue
        }
    }
    
    EdgeFadeWrapper(
        orientation = Orientation.Vertical,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
    ) {
        Column(
            modifier = modifier
                .verticalScroll(state),
            verticalArrangement = Arrangement.spacedBy(verticalItemSpacing)
        ) {
            content()
        }
    }
}

@Composable
fun EdgeFadeRow(
    modifier: Modifier = Modifier,
    state: ScrollState,
    horizontalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.value > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            state.value < state.maxValue
        }
    }
    
    EdgeFadeWrapper(
        orientation = Orientation.Horizontal,
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
    ) {
        Row(
            modifier = modifier
                .horizontalScroll(state),
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
        ) {
            content()
        }
    }
}


// LAZY LISTS

@Composable
fun EdgeFadeIndexedLazyColumn(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState,
    verticalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: IndexedLazyListScope.() -> Unit
) {
    val listState = state.listState
    
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
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize
    ) {
        IndexedLazyColumn(
            modifier = modifier,
            state = state,
            verticalArrangement = Arrangement.spacedBy(verticalItemSpacing)
        ) {
            item {
                Spacer(Modifier)
            }
            
            content()
            
            item {
                Spacer(Modifier)
            }
        }
    }
}

@Composable
fun EdgeFadeIndexedLazyRow(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState,
    horizontalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: IndexedLazyListScope.() -> Unit
) {
    val listState = state.listState
    
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
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Horizontal,
        fadeColor = fadeColor,
        fadeSize = fadeSize
    ) {
        IndexedLazyRow(
            modifier = modifier,
            state = state,
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
        ) {
            item {
                Spacer(Modifier)
            }
            
            content()
            
            item {
                Spacer(Modifier)
            }
        }
    }
}

@Composable
fun EdgeFadeLazyVerticalGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState,
    columns: Int,
    verticalItemSpacing: Dp = 0.dp,
    horizontalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: LazyGridScope.() -> Unit
) {
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
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
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize
    ) {
        LazyVerticalGrid(
            modifier = modifier,
            state = state,
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Spacer(Modifier)
            }
            
            content()
            
            item(span = { GridItemSpan(columns) }) {
                Spacer(Modifier)
            }
        }
    }
}

@Composable
fun EdgeFadeLazyStaggeredVerticalGrid(
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState,
    columns: Int,
    verticalItemSpacing: Dp = 0.dp,
    horizontalItemSpacing: Dp = 0.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    content: LazyStaggeredGridScope.() -> Unit
) {
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
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
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize
    ) {
        LazyVerticalStaggeredGrid(
            modifier = modifier,
            state = state,
            columns = StaggeredGridCells.Fixed(columns),
            verticalItemSpacing = verticalItemSpacing,
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier)
            }
            
            content()
            
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier)
            }
        }
    }
}