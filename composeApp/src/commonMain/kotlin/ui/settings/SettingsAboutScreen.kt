package ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.License
import data.MainViewModel
import data.browseWeb
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_about_build
import foundation.composeapp.generated.resources.app_about_version
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.developer_name
import foundation.composeapp.generated.resources.settings_about_licenses_title
import foundation.composeapp.generated.resources.settings_about_licenses_unknown
import org.jetbrains.compose.resources.stringResource
import ui.ClickableIcon
import ui.EdgeFadeLazyList
import ui.HintText
import ui.HorizontalSeparator
import ui.LinkButton
import ui.OptionDetailText
import ui.SmallText
import ui.SubtitleText
import ui.TitleText
import ui.consumeClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsAboutScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val licenses by viewModel.licenses.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        val listState = rememberLazyListState()
        
        EdgeFadeLazyList(
            modifier = Modifier
                .fillMaxWidth(),
            listState = listState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                state = listState,
                contentPadding = PaddingValues(
                    vertical = 16.dp,
                    horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 16.dp)
                        ) {
                            TitleText(
                                text = stringResource(Res.string.app_name),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 16.dp)
                            )
                            
                            HorizontalSeparator()
                            
                            OptionDetailText(
                                modifier = Modifier.clickable {
                                    onVibrate()
                                    browseWeb("https://www.youtube.com/shorts/lCJo8vhmIdc")
                                },
                                title = stringResource(Res.string.app_about_version),
                                subtitle = viewModel.platform.version,
                            )
                            
                            OptionDetailText(
                                title = stringResource(Res.string.app_about_build),
                                subtitle = viewModel.platform.build,
                            )
                        }
                    }
                }
                
                item {
                    TitleText(
                        text = stringResource(Res.string.settings_about_licenses_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
                
                licenses.mapping.keys.forEach { sortedLicenseUrl ->
                    val licenseList = licenses.mapping[sortedLicenseUrl] ?: listOf()
                    val licenseTitle = licenseList.first().moduleLicense
                    val licenseUrl = licenseList.first().moduleLicenseUrl
                    
                    stickyHeader {
                        LicenseHeader(
                            backgroundColor = MaterialTheme.colorScheme.background,
                            moduleLicense = licenseTitle,
                            moduleLicenseUrl = licenseUrl,
                            onViewLicenseUrl = {
                                onVibrate()
                                browseWeb(licenseUrl)
                            }
                        )
                    }
                    
                    itemsIndexed(items = licenseList) { index, license ->
                        LicenseCard(
                            license = license,
                            onViewModuleUrl = { url ->
                                onVibrate()
                                browseWeb(url)
                            }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
        
        BridgeButton(
            onVibrate = onVibrate
        )
    }
}

@Composable
fun LicenseCard(
    license: License,
    onViewModuleUrl: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .minimumInteractiveComponentSize(),
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
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                text = license.moduleName + ":" + license.moduleVersion
            )
        }
    }
}

@Composable
fun LicenseHeader(
    backgroundColor: Color,
    moduleLicense: String,
    moduleLicenseUrl: String,
    onViewLicenseUrl: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            SubtitleText(
                text = moduleLicense.ifBlank { stringResource(Res.string.settings_about_licenses_unknown) }
            )
            
            if (moduleLicenseUrl.isNotBlank()) {
                SmallText(
                    text = moduleLicenseUrl
                )
            }
        }
        
        if (onViewLicenseUrl != null) {
            ClickableIcon(
                onClick = onViewLicenseUrl,
                imageVector = Icons.Default.Link,
                contentDescription = moduleLicense
            )
        }
    }
}

@Composable
fun BridgeButton(
    modifier: Modifier = Modifier,
    onVibrate: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .consumeClick()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        LinkButton(
            text = stringResource(Res.string.developer_name),
            url = "https://bridge.supplies/",
            onVibrate = onVibrate
        )
    }
}