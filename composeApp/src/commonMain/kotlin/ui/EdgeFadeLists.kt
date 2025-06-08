package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// REMEMBERS

@Composable
fun rememberNestedScrollConnection(
    firstVisibleItemScrollOffset: () -> Int,
    onScrollUp: (Boolean) -> Unit
): NestedScrollConnection {
    return remember {
        object : NestedScrollConnection {
            private var scrollOffset = 0f
            
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val newScrollOffset = -(firstVisibleItemScrollOffset()).toFloat()
                val actualDelta = newScrollOffset - scrollOffset
                
                if (available.y >= 1) {
                    onScrollUp(true)
                } else if (actualDelta < 0f) {
                    onScrollUp(false)
                }
                
                scrollOffset = newScrollOffset
                return Offset.Zero
            }
        }
    }
}

// WRAPPER

@Composable
fun EdgeFadeWrapper(
    orientation: Orientation,
    showStartEdgeFade: Boolean,
    showEndEdgeFade: Boolean,
    useIntrinsicHeight: Boolean = true, // for horizontal layouts
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    onScrollForward: ((forward: Boolean) -> Unit)? = null,
    wrappedContent: @Composable BoxScope.() -> Unit
) {
    val modifier = when {
        orientation == Orientation.Vertical ->
            Modifier.wrapContentHeight()
        
        orientation == Orientation.Horizontal && !useIntrinsicHeight ->
            Modifier.wrapContentWidth()
        
        else ->
            Modifier.wrapContentWidth().height(IntrinsicSize.Max)
    }
    
    Box(
        modifier = modifier
    ) {
        wrappedContent()
        
        if (orientation == Orientation.Vertical) {
            AnimatedVerticalEdgeFade(
                isVisible = showStartEdgeFade,
                isStartEdge = true,
                fadeColor = fadeColor,
                fadeSize = fadeSize,
                onScrollForward = onScrollForward
            )
            
            AnimatedVerticalEdgeFade(
                isVisible = showEndEdgeFade,
                isStartEdge = false,
                fadeColor = fadeColor,
                fadeSize = fadeSize,
                onScrollForward = onScrollForward
            )
        } else {
            AnimatedHorizontalEdgeFade(
                isVisible = showStartEdgeFade,
                isStartEdge = true,
                fadeColor = fadeColor,
                fadeSize = fadeSize,
                onScrollForward = onScrollForward
            )
            
            AnimatedHorizontalEdgeFade(
                isVisible = showEndEdgeFade,
                isStartEdge = false,
                fadeColor = fadeColor,
                fadeSize = fadeSize,
                onScrollForward = onScrollForward
            )
        }
    }
}

@Composable
fun BoxScope.AnimatedVerticalEdgeFade(
    isVisible: Boolean,
    isStartEdge: Boolean,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp,
    scrollFabSize: Dp = 36.dp,
    onScrollForward: ((forward: Boolean) -> Unit)? = null
) {
    val alignment = if (isStartEdge) {
        Alignment.TopCenter
    } else {
        Alignment.BottomCenter
    }
    
    val heightMod = if (onScrollForward != null) {
        Modifier
            .defaultMinSize(minHeight = scrollFabSize)
            .heightIn(max = fadeSize)
    } else {
        Modifier
            .height(fadeSize)
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { if (isStartEdge) -it else it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { if (isStartEdge) -it else it }),
        modifier = heightMod
            .align(alignment)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isStartEdge) fadeColor else Color.Transparent,
                            if (isStartEdge) Color.Transparent else fadeColor
                        )
                    )
                )
                .then(
                    if (onScrollForward != null)
                        Modifier.clickable {
                            onScrollForward(isStartEdge)
                        } else Modifier
                )
        ) {
            onScrollForward?.let {
                SmallFab(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(scrollFabSize)
                        .padding(4.dp),
                    icon = {
                        Icon(
                            imageVector = if (isStartEdge) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            },
                            contentDescription = if (isStartEdge) {
                                "Back"
                            } else {
                                "Forward"
                            }
                        )
                    },
                    containerColor = fadeColor
                ) {
                    onScrollForward(isStartEdge)
                }
            }
        }
    }
}

@Composable
fun BoxScope.AnimatedHorizontalEdgeFade(
    isVisible: Boolean,
    isStartEdge: Boolean,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp,
    scrollFabSize: Dp = 36.dp,
    onScrollForward: ((forward: Boolean) -> Unit)? = null
) {
    val alignment = if (isStartEdge) {
        Alignment.CenterStart
    } else {
        Alignment.CenterEnd
    }
    
    val widthMod = if (onScrollForward != null) {
        Modifier
            .defaultMinSize(minWidth = scrollFabSize)
            .widthIn(max = fadeSize)
    } else {
        Modifier
            .width(fadeSize)
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { if (isStartEdge) -it else it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { if (isStartEdge) -it else it }),
        modifier = widthMod
            .align(alignment)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            if (isStartEdge) fadeColor else Color.Transparent,
                            if (isStartEdge) Color.Transparent else fadeColor
                        )
                    )
                )
                .then(
                    if (onScrollForward != null)
                        Modifier.clickable {
                            onScrollForward(isStartEdge)
                        } else Modifier
                )
        ) {
            onScrollForward?.let {
                SmallFab(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(scrollFabSize)
                        .padding(4.dp),
                    icon = {
                        Icon(
                            imageVector = if (isStartEdge) {
                                Icons.AutoMirrored.Default.ArrowBack
                            } else {
                                Icons.AutoMirrored.Default.ArrowForward
                            },
                            contentDescription = if (isStartEdge) {
                                "Back"
                            } else {
                                "Forward"
                            }
                        )
                    }
                ) {
                    onScrollForward(isStartEdge)
                }
            }
        }
    }
}


// STATIC LISTS

@Composable
fun EdgeFadeColumn(
    modifier: Modifier = Modifier,
    state: ScrollState,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verAlignment: Alignment.Vertical = Alignment.Top,
    // spacings
    startSpacing: Dp = 8.dp, // min: itemSpacing
    itemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: itemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    state.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    state.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
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
        onScrollForward = onScrollForwardInternal
    ) {
        Column(
            modifier = modifier
                .verticalScroll(state)
                .onGloballyPositioned { containerSize = it.size.height },
            verticalArrangement = Arrangement.spacedBy(itemSpacing, verAlignment),
            horizontalAlignment = horAlignment
        ) {
            if (startSpacing >= itemSpacing) {
                HorizontalSpacer(startSpacing - itemSpacing)
            }
            
            content()
            
            if (endSpacing >= itemSpacing) {
                HorizontalSpacer(endSpacing - itemSpacing)
            }
        }
    }
}

@Composable
fun EdgeFadeRow(
    modifier: Modifier = Modifier,
    state: ScrollState,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.Start,
    verAlignment: Alignment.Vertical = Alignment.CenterVertically,
    // spacings
    startSpacing: Dp = 8.dp, // min: itemSpacing
    itemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: itemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: @Composable RowScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    state.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    state.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
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
        onScrollForward = onScrollForwardInternal
    ) {
        Row(
            modifier = modifier
                .horizontalScroll(state)
                .onGloballyPositioned {
                    containerSize = it.parentLayoutCoordinates?.size?.width ?: it.size.width
                },
            horizontalArrangement = Arrangement.spacedBy(itemSpacing, horAlignment),
            verticalAlignment = verAlignment
        ) {
            if (startSpacing >= itemSpacing) {
                VerticalSpacer(startSpacing - itemSpacing)
            }
            
            content()
            
            if (endSpacing >= itemSpacing) {
                VerticalSpacer(endSpacing - itemSpacing)
            }
        }
    }
}


// LAZY LISTS

@Composable
fun EdgeFadeIndexedLazyColumn(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.Start,
    verAlignment: Alignment.Vertical = Alignment.CenterVertically,
    // spacings
    startSpacing: Dp = 8.dp, // min: itemSpacing
    itemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: itemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: IndexedLazyListScope.() -> Unit
) {
    val listState = state.listState
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    listState.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    listState.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            listState.canScrollBackward
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
        onScrollForward = onScrollForwardInternal
    ) {
        IndexedLazyColumn(
            modifier = modifier
                .onGloballyPositioned { containerSize = it.size.height },
            state = state,
            verticalArrangement = Arrangement.spacedBy(itemSpacing, verAlignment),
            horizontalAlignment = horAlignment
        ) {
            if (startSpacing >= itemSpacing) {
                item {
                    HorizontalSpacer(startSpacing - itemSpacing)
                }
            }
            
            content()
            
            if (endSpacing >= itemSpacing) {
                item {
                    HorizontalSpacer(endSpacing - itemSpacing)
                }
            }
        }
    }
}

@Composable
fun EdgeFadeIndexedLazyRow(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.Start,
    verAlignment: Alignment.Vertical = Alignment.CenterVertically,
    // spacings
    startSpacing: Dp = 8.dp, // min: itemSpacing
    itemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: itemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: IndexedLazyListScope.() -> Unit
) {
    val listState = state.listState
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    listState.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    listState.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            listState.canScrollBackward
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Horizontal,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
        onScrollForward = onScrollForwardInternal
    ) {
        IndexedLazyRow(
            modifier = modifier
                .onGloballyPositioned { containerSize = it.size.width },
            state = state,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing, horAlignment),
            verticalAlignment = verAlignment
        ) {
            if (startSpacing >= itemSpacing) {
                item {
                    VerticalSpacer(startSpacing - itemSpacing)
                }
            }
            
            content()
            
            if (endSpacing >= itemSpacing) {
                item {
                    VerticalSpacer(endSpacing - itemSpacing)
                }
            }
        }
    }
}

@Composable
fun EdgeFadeLazyVerticalGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState,
    columns: Int,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verAlignment: Alignment.Vertical = Alignment.Top,
    // spacings
    startSpacing: Dp = 8.dp, // min: verticalItemSpacing
    verticalItemSpacing: Dp = 8.dp,
    horizontalItemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: verticalItemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: LazyGridScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    state.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    state.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.canScrollBackward
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            state.canScrollForward
        }
    }
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
        onScrollForward = onScrollForwardInternal
    ) {
        LazyVerticalGrid(
            modifier = modifier
                .onGloballyPositioned { containerSize = it.size.height },
            state = state,
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(verticalItemSpacing, verAlignment),
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing, horAlignment)
        ) {
            if (startSpacing >= verticalItemSpacing) {
                item(span = { GridItemSpan(columns) }) {
                    HorizontalSpacer(startSpacing - verticalItemSpacing)
                }
            }
            
            content()
            
            if (endSpacing >= verticalItemSpacing) {
                item(span = { GridItemSpan(columns) }) {
                    HorizontalSpacer(endSpacing - verticalItemSpacing)
                }
            }
        }
    }
}

@Composable
fun EdgeFadeLazyStaggeredVerticalGrid(
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState,
    columns: Int,
    // alignments
    horAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    // spacings
    startSpacing: Dp = 8.dp, // min: verticalItemSpacing
    verticalItemSpacing: Dp = 8.dp,
    horizontalItemSpacing: Dp = 8.dp,
    endSpacing: Dp = 8.dp, // min: verticalItemSpacing
    // edge fades
    fadeColor: Color = MaterialTheme.colorScheme.background,
    fadeSize: Dp = 16.dp,
    scrollPercent: Float = 0.65f,
    onScrollForward: (() -> Unit)? = null, // include to activate scroll helpers
    content: LazyStaggeredGridScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(0) }
    
    val onScrollForwardInternal: ((Boolean) -> Unit)? = if (onScrollForward != null) {
        { scrollForward ->
            onScrollForward.invoke()
            
            coroutineScope.launch {
                if (scrollForward) {
                    state.animateScrollBy(-containerSize.toFloat() * scrollPercent)
                } else {
                    state.animateScrollBy(containerSize.toFloat() * scrollPercent)
                }
            }
        }
    } else null
    
    val showStartEdgeFade by remember {
        derivedStateOf {
            state.canScrollBackward
        }
    }
    
    val showEndEdgeFade by remember {
        derivedStateOf {
            state.canScrollForward
        }
    }
    
    EdgeFadeWrapper(
        showStartEdgeFade = showStartEdgeFade,
        showEndEdgeFade = showEndEdgeFade,
        orientation = Orientation.Vertical,
        fadeColor = fadeColor,
        fadeSize = fadeSize,
        onScrollForward = onScrollForwardInternal
    ) {
        LazyVerticalStaggeredGrid(
            modifier = modifier
                .onGloballyPositioned { containerSize = it.size.height },
            state = state,
            columns = StaggeredGridCells.Fixed(columns),
            verticalItemSpacing = verticalItemSpacing,
            horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing, horAlignment)
        ) {
            if (startSpacing >= verticalItemSpacing) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    HorizontalSpacer(startSpacing - verticalItemSpacing)
                }
            }
            
            content()
            
            if (endSpacing >= verticalItemSpacing) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    HorizontalSpacer(endSpacing - verticalItemSpacing)
                }
            }
        }
    }
}