package ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_settings_scroll_helpers_subtitle
import foundation.composeapp.generated.resources.app_settings_scroll_helpers_title
import org.jetbrains.compose.resources.stringResource
import ui.BodyText
import ui.EdgeFadeIndexedLazyRow
import ui.EdgeFadeRow
import ui.SettingsSwitch
import ui.TitledCard
import ui.VerticalSpacer
import ui.rememberIndexedLazyListState

@Composable
fun HomeRowsScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val useScrollHelpers by viewModel.useScrollHelpers.collectAsState()
    var isLazy by rememberSaveable { mutableStateOf(false) }
    
    val onScrollForward = { ->
        hapticFeedback()
    }
    
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
        
        SettingsSwitch(
            modifier = Modifier.padding(horizontal = 8.dp),
            title = stringResource(Res.string.app_settings_scroll_helpers_title),
            subtitle = stringResource(Res.string.app_settings_scroll_helpers_subtitle),
            enabled = useScrollHelpers
        ) {
            viewModel.useScrollHelpers(it)
            hapticFeedback()
        }
        
        Crossfade(
            modifier = Modifier
                .wrapContentHeight(),
            targetState = isLazy
        ) { lazy ->
            if (!lazy) {
                val state = rememberScrollState()
                EdgeFadeRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    state = state,
                    itemSpacing = 8.dp,
                    onScrollForward = if (useScrollHelpers) onScrollForward else null
                ) {
                    VerticalSpacer()
                    
                    repeat(10) { index ->
                        TitledCard(
                            modifier = Modifier
                                .width(200.dp)
                                .padding(vertical = 8.dp)
                                .fillMaxHeight(),
                            title = "Card $index",
                            subtitle = "Subtitle $index"
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                BodyText(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    text = "This item has been loaded statically and remains in memory."
                                )
                            }
                        }
                    }
                    
                    VerticalSpacer()
                }
            } else {
                val state = rememberIndexedLazyListState()
                EdgeFadeIndexedLazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    state = state,
                    itemSpacing = 8.dp,
                    onScrollForward = if (useScrollHelpers) onScrollForward else null
                ) {
                    item { VerticalSpacer() }
                    
                    repeat(100) { index ->
                        item {
                            TitledCard(
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(vertical = 8.dp)
                                    .fillMaxHeight(),
                                title = "Card $index",
                                subtitle = "Subtitle $index"
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    BodyText(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        text = "This item has been loaded statically and remains in memory."
                                    )
                                }
                            }
                        }
                    }
                    
                    item { VerticalSpacer() }
                }
            }
        }
    }
}