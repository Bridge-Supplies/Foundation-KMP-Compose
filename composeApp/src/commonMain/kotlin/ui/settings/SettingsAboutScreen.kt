package ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import ui.EdgeFadeLazyList
import ui.LinkButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsAboutScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val licenses by viewModel.licenses.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val listState = rememberLazyListState()
        
        EdgeFadeLazyList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            listState = listState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Start,
                        text = stringResource(Res.string.app_name),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                item {
                    val text = stringResource(Res.string.app_about_version, viewModel.platform.version) + "\n" +
                        stringResource(Res.string.app_about_build, viewModel.platform.build)
                    
                    Text(
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                
                item {
                    LinkButton(
                        text = stringResource(Res.string.developer_name),
                        url = "https://bridge.supplies/",
                        onVibrate = onVibrate
                    )
                }
                
                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Start,
                        text = stringResource(Res.string.settings_about_licenses_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
                
                licenses.mapping.keys.forEach { sortedLicenseUrl ->
                    val licenseList = licenses.mapping[sortedLicenseUrl] ?: listOf()
                    val licenseTitle = licenseList.first().moduleLicense
                    val licenseUrl = licenseList.first().moduleLicenseUrl
                    
                    stickyHeader {
                        val onViewLicenseUrl: (() -> Unit)? =
                            if (licenseUrl.isNotEmpty()) {
                                {
                                    browseWeb(licenseUrl)
                                }
                            } else null
                        
                        LicenseHeader(
                            backgroundColor = MaterialTheme.colorScheme.background,
                            moduleLicense = licenseTitle,
                            moduleLicenseUrl = licenseUrl,
                            onViewLicenseUrl = onViewLicenseUrl
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LicenseCard(
    license: License,
    onViewModuleUrl: (String) -> Unit
) {
    val cardModifier = if (license.moduleUrl.isNotBlank()) {
        Modifier.clickable {
            onViewModuleUrl(license.moduleUrl)
        }
    } else {
        Modifier
    }
    
    Card(
        modifier = cardModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = license.moduleName + ":" + license.moduleVersion,
                style = MaterialTheme.typography.bodySmall
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
                .padding(vertical = 8.dp)
        ) {
            Text(
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
                text = moduleLicense.ifBlank { stringResource(Res.string.settings_about_licenses_unknown) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
            
            if (moduleLicenseUrl.isNotBlank()) {
                Text(
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Start,
                    text = moduleLicenseUrl,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
            }
        }
        
        if (onViewLicenseUrl != null) {
            IconButton(
                onClick = {
                    onViewLicenseUrl()
                }
            ) {
                Icon(Icons.Default.Link, contentDescription = moduleLicense)
            }
        }
    }
}