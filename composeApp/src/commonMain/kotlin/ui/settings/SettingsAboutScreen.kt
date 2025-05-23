package ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import data.License
import data.MainViewModel
import data.browseWeb
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_about_build
import foundation.composeapp.generated.resources.app_about_version
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.settings_about_licenses_unknown
import foundation.composeapp.generated.resources.tag_built_with_foundation
import org.jetbrains.compose.resources.stringResource
import ui.BottomButton
import ui.ClickableIcon
import ui.EdgeFadeIndexedLazyColumn
import ui.HintText
import ui.OptionDetailText
import ui.StickyHeader
import ui.TitledCard
import ui.rememberIndexedLazyListState
import ui.rememberNestedScrollConnection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsAboutScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val horPaddingMod = Modifier.padding(horizontal = 16.dp)
    val licenses by viewModel.licenses.collectAsState()
    val listState = rememberIndexedLazyListState()
    
    var bottomButtonsVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection(
        firstVisibleItemScrollOffset = { listState.listState.firstVisibleItemScrollOffset }
    ) { scrollUp ->
        bottomButtonsVisible = scrollUp
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        EdgeFadeIndexedLazyColumn(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection),
            state = listState,
            itemSpacing = 8.dp
        ) {
            item {
                TitledCard(
                    modifier = horPaddingMod,
                    title = stringResource(Res.string.app_name)
                ) {
                    OptionDetailText(
                        modifier = Modifier
                            .clickable {
                                hapticFeedback()
                                browseWeb("https://www.youtube.com/shorts/lCJo8vhmIdc")
                            }
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        title = stringResource(Res.string.app_about_version),
                        subtitle = viewModel.platform.version,
                    )
                    
                    OptionDetailText(
                        modifier = Modifier
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        title = stringResource(Res.string.app_about_build),
                        subtitle = viewModel.platform.build,
                    )
                    
                    Spacer(Modifier.height(8.dp))
                }
            }
            
            licenses.mapping.keys.forEach { sortedLicenseUrl ->
                val licenseList = licenses.mapping[sortedLicenseUrl] ?: listOf()
                
                stickyHeader {
                    val licenseUrl = licenseList.first().moduleLicenseUrl
                    val licenseTitle = licenseList.first().moduleLicense.ifBlank {
                        stringResource(Res.string.settings_about_licenses_unknown)
                    }
                    
                    StickyHeader(
                        modifier = horPaddingMod,
                        titleText = licenseTitle,
                        subtitleText = licenseUrl.ifBlank { null }
                    ) {
                        if (licenseUrl.isNotBlank()) {
                            ClickableIcon(
                                imageVector = Icons.Default.Link,
                                contentDescription = licenseTitle
                            ) {
                                hapticFeedback()
                                browseWeb(licenseUrl)
                            }
                        }
                    }
                }
                
                itemsIndexed(items = licenseList) { index, license ->
                    LicenseCard(
                        modifier = horPaddingMod,
                        license = license,
                        onViewModuleUrl = { url ->
                            hapticFeedback()
                            browseWeb(url)
                        }
                    )
                }
            }
            
            item { Spacer(Modifier.minimumInteractiveComponentSize()) }
        }
        
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            visible = bottomButtonsVisible,
            enter = slideInVertically(initialOffsetY = { it * 2 }),
            exit = slideOutVertically(targetOffsetY = { it * 2 }),
        ) {
            BottomButton(
                text = stringResource(Res.string.tag_built_with_foundation),
                paddingValues = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            ) {
                hapticFeedback()
                browseWeb("https://github.com/Bridge-Supplies/Foundation-KMP-Compose")
            }
        }
    }
}

@Composable
fun LicenseCard(
    modifier: Modifier = Modifier,
    license: License,
    onViewModuleUrl: (String) -> Unit
) {
    Card(
        modifier = modifier,
        onClick = {
            if (license.moduleUrl.isNotBlank()) {
                onViewModuleUrl(license.moduleUrl)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .minimumInteractiveComponentSize(),
            verticalArrangement = Arrangement.Center
        ) {
            HintText(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                text = license.moduleName + ":" + license.moduleVersion,
                maxLines = 1
            )
        }
    }
}