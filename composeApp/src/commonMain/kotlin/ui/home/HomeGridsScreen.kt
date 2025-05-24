package ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.MainViewModel
import ui.BodyText
import ui.EdgeFadeLazyStaggeredVerticalGrid
import ui.EdgeFadeLazyVerticalGrid
import ui.SettingsSwitch
import ui.TitledCard

@Composable
fun HomeGridsScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    var isStaggered by rememberSaveable { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            title = "Staggered grid",
            subtitle = "Don't force content cards to align sizes",
            enabled = isStaggered
        ) { enabled ->
            isStaggered = enabled
            hapticFeedback()
        }
        
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = isStaggered
        ) { staggered ->
            if (!staggered) {
                val state = rememberLazyGridState()
                
                EdgeFadeLazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = state,
                    columns = if (isPortraitMode) 2 else 3,
                    verticalItemSpacing = 8.dp,
                    horizontalItemSpacing = 0.dp
                ) {
                    repeat(10) { index ->
                        val text = when (index % 3) {
                            0 -> "This card will stay the same size."
                            1 -> "This card has a bit more text, but will still stay the same size."
                            else -> "Even with a lot of text, this card will still conform to the same size."
                        }
                        
                        item {
                            TitledCard(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(horizontal = 8.dp),
                                title = "Grid Card $index",
                                subtitle = "Subtitle $index"
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp)
                                ) {
                                    BodyText(
                                        text = text
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val state = rememberLazyStaggeredGridState()
                EdgeFadeLazyStaggeredVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = state,
                    columns = if (isPortraitMode) 2 else 3,
                    verticalItemSpacing = 8.dp,
                    horizontalItemSpacing = 0.dp
                ) {
                    repeat(100) { index ->
                        val text = when (index % 3) {
                            0 -> "This card will wrap the size of its contents."
                            1 -> "This card has just a bit more text and will grow accordingly."
                            else -> "Staggered grids allow items of varying heights to fit together efficiently."
                        }
                        
                        item {
                            TitledCard(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                title = "Staggered $index",
                                subtitle = "Subtitle $index"
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp)
                                ) {
                                    BodyText(
                                        text = text
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