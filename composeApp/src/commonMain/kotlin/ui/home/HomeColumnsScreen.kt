package ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MainViewModel
import ui.BodyText
import ui.EdgeFadeColumn
import ui.EdgeFadeIndexedLazyColumn
import ui.ExpandableTitledCard
import ui.SettingsSwitch
import ui.rememberIndexedLazyListState

@Composable
fun HomeColumnsScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    var isLazy by rememberSaveable { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            title = "Lazy loading",
            subtitle = "Load content cards lazily to save on memory usage",
            enabled = isLazy
        ) { enabled ->
            isLazy = enabled
            hapticFeedback()
        }
        
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = isLazy
        ) { lazy ->
            if (!lazy) {
                val state = rememberScrollState()
                val expandedCards = rememberSaveable { mutableStateOf(listOf<Int>()) }
                
                EdgeFadeColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    endSpacing = 16.dp
                ) {
                    repeat(10) { index ->
                        ExpandableTitledCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            title = "Card $index",
                            isExpanded = index in expandedCards.value,
                            onExpand = { isExpanded ->
                                if (isExpanded) {
                                    expandedCards.value = expandedCards.value + index
                                } else {
                                    expandedCards.value = expandedCards.value - index
                                }
                            }
                        ) { isExpanded ->
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                BodyText(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    text = "This item has been loaded statically and remains in memory." + if (isExpanded) "\n\nThis item is expanded." else ""
                                )
                            }
                        }
                    }
                }
            } else {
                val state = rememberIndexedLazyListState()
                val expandedCards = rememberSaveable { mutableStateOf(listOf<Int>()) }
                
                EdgeFadeIndexedLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    endSpacing = 16.dp
                ) {
                    repeat(100) { index ->
                        item {
                            ExpandableTitledCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                title = "Card $index",
                                isExpanded = index in expandedCards.value,
                                onExpand = { isExpanded ->
                                    if (isExpanded) {
                                        expandedCards.value = expandedCards.value + index
                                    } else {
                                        expandedCards.value = expandedCards.value - index
                                    }
                                }
                            ) { isExpanded ->
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    BodyText(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        text = "This item has been loaded lazily and will leave memory when off screen." + if (isExpanded) "\n\nThis item is expanded." else ""
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}