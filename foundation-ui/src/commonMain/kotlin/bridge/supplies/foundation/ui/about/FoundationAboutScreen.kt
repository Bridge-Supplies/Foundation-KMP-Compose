package bridge.supplies.foundation.ui.about

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.platform.openUrl
import bridge.supplies.foundation.ui.components.BottomButton
import bridge.supplies.foundation.ui.components.ClickableIcon
import bridge.supplies.foundation.ui.components.EdgeFadeIndexedLazyColumn
import bridge.supplies.foundation.ui.components.HintText
import bridge.supplies.foundation.ui.components.OptionDetailText
import bridge.supplies.foundation.ui.components.StickyHeader
import bridge.supplies.foundation.ui.components.TitledCard
import bridge.supplies.foundation.ui.components.rememberIndexedLazyListState
import bridge.supplies.foundation.ui.components.rememberNestedScrollConnection
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.app_about_build
import bridge.supplies.foundation.ui.resources.app_about_version
import bridge.supplies.foundation.ui.resources.settings_about_licenses_unknown
import bridge.supplies.foundation.ui.resources.tag_built_with_foundation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

private const val FOUNDATION_URL = "https://github.com/Bridge-Supplies/Foundation-KMP-Compose"

public class AboutConfig(
    /**
     * The license report JSON generated in the app's shared module (jk1 license-report), e.g.
     * `{ Res.readBytes("files/json/licenses.json") }`. It covers Foundation's dependencies and the app's.
     */
    public val licensesJson: suspend () -> ByteArray,
    /** Called when the version row is tapped (an easter egg, for example). */
    public val onVersionClick: (() -> Unit)? = null,
    /** Extra rows in the app card, below version and build. */
    public val extraContent: (@Composable () -> Unit)? = null,
)

/** App name, version and build, then the open source licenses grouped by license, and a "Built with Foundation" link. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FoundationAboutScreen(
    config: AboutConfig
) {
    val horPaddingMod = Modifier.padding(horizontal = 16.dp)
    val viewModel = foundationViewModel()
    val hapticFeedback = rememberHapticFeedback()
    val licenses by produceState(LicenseMap()) {
        value = withContext(Dispatchers.Default) { loadLicenses(config.licensesJson()) }
    }
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
            endSpacing = 56.dp
        ) {
            item {
                TitledCard(
                    modifier = horPaddingMod,
                    title = viewModel.config.appName
                ) {
                    OptionDetailText(
                        modifier = Modifier
                            .then(
                                if (config.onVersionClick != null) {
                                    Modifier.clickable {
                                        hapticFeedback()
                                        config.onVersionClick.invoke()
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        title = stringResource(Res.string.app_about_version),
                        subtitle = viewModel.config.version,
                    )
                    
                    OptionDetailText(
                        modifier = Modifier
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        title = stringResource(Res.string.app_about_build),
                        subtitle = viewModel.config.build,
                    )
                    
                    config.extraContent?.invoke()
                    
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
                                openUrl(licenseUrl)
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
                            openUrl(url)
                        }
                    )
                }
            }
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
                openUrl(FOUNDATION_URL)
            }
        }
    }
}

@Composable
private fun LicenseCard(
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


// Licenses (jk1 license-report JSON)

@Serializable
private data class License(
    val moduleName: String = "",
    val moduleUrl: String = "",
    val moduleVersion: String = "",
    val moduleLicense: String = "",
    val moduleLicenseUrl: String = ""
)

@Serializable
private data class LicenseList(
    @SerialName("dependencies") val licenses: List<License> = emptyList()
)

private data class LicenseMap(
    val mapping: Map<String, List<License>> = emptyMap()
)

private val licenseJson = Json { ignoreUnknownKeys = true }

private fun loadLicenses(bytes: ByteArray): LicenseMap = LicenseMap(
    licenseJson.decodeFromString<LicenseList>(bytes.decodeToString()).licenses
        .sortedBy {
            it.moduleName
        }
        .groupBy {
            it.moduleLicenseUrl
                .replaceFirst(oldValue = "https://", newValue = "")
                .replaceFirst(oldValue = "http://", newValue = "")
        }
)
