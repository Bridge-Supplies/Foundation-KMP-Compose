package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// IndexedLazyColumn: https://gist.github.com/DantheCodingGui/e660e987685b11d2e94af3e94d76797c

@Composable
fun IndexedLazyColumn(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState = rememberIndexedLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: IndexedLazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state.listState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        IndexedLazyListScope(this, state).content()
    }
}

@Composable
fun IndexedLazyRow(
    modifier: Modifier = Modifier,
    state: IndexedLazyListState = rememberIndexedLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: IndexedLazyListScope.() -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = state.listState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        IndexedLazyListScope(this, state).content()
    }
}

@Composable
fun rememberIndexedLazyListState(
    listState: LazyListState = rememberLazyListState(),
): IndexedLazyListState {
    return remember { IndexedLazyListState(listState) }
}

@Stable
class IndexedLazyListState internal constructor(
    val listState: LazyListState,
) {
    val itemIndexMapping = mutableStateMapOf<Any, Int>()
    
    internal fun linkKeyToIndex(key: Any, index: Int) {
        itemIndexMapping[key] = index
    }
}

@Stable
class IndexedLazyListScope(
    private val lazyListScope: LazyListScope,
    private val state: IndexedLazyListState,
) : LazyListScope {
    
    private var currentItemLayoutIndex = 0
    
    override fun item(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) {
        lazyListScope.item(key, contentType, content)
        if (key != null)
            state.linkKeyToIndex(key, currentItemLayoutIndex)
        currentItemLayoutIndex++
    }
    
    @Deprecated("Use the non deprecated overload", level = DeprecationLevel.HIDDEN)
    override fun item(key: Any?, content: @Composable LazyItemScope.() -> Unit) {
        item(key, null, content)
    }
    
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit
    ) {
        lazyListScope.items(count, key, contentType, itemContent)
        
        repeat(count) { i ->
            val resolvedKey = key?.invoke(i)
            if (resolvedKey != null)
                state.linkKeyToIndex(resolvedKey, currentItemLayoutIndex)
            currentItemLayoutIndex++
        }
    }
    
    @Deprecated("Use the non deprecated overload", level = DeprecationLevel.HIDDEN)
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit
    ) {
        items(count, key, { null }, itemContent)
    }
    
    @ExperimentalFoundationApi
    override fun stickyHeader(key: Any?, contentType: Any?, content: @Composable LazyItemScope.(Int) -> Unit) {
        lazyListScope.stickyHeader(key, contentType, content)
        if (key != null)
            state.linkKeyToIndex(key, currentItemLayoutIndex)
        currentItemLayoutIndex++
    }
}