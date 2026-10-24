package ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.platform.PlatformFeature
import bridge.supplies.foundation.ui.components.FloatingButton
import bridge.supplies.foundation.ui.components.TextInput
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.isPortrait
import bridge.supplies.foundation.ui.qr.ShareQrCode
import bridge.supplies.foundation.ui.settings.FoundationSettingKeys
import bridge.supplies.foundation.ui.sheets.ShareAppSheet
import bridge.supplies.foundation.ui.sheets.sheetController
import bridge.supplies.foundation.ui.shell.TopBarAction
import bridge.supplies.foundation.ui.shell.TopBarActions
import data.MainViewModel
import data.SharedData
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.action_share
import foundation.composeapp.generated.resources.generate_qr_code_encrypted_text
import foundation.composeapp.generated.resources.generate_qr_code_text
import foundation.composeapp.generated.resources.scanner_button_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShareCodeScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val sheets = sheetController()
    val isPortraitMode = isPortrait()
    val sharedText by viewModel.sharedText.collectAsState()
    val foundation = foundationViewModel()
    val useEncryptedShare by foundation.setting(FoundationSettingKeys.encryptedShare).collectAsState()
    val sharedData = sharedText.takeIf { it.isNotBlank() }?.let { SharedData(it) }
    
    val onUpdateSharedText = { newText: String ->
        viewModel.setSharedText(newText)
    }
    
    val onClickScanner = {
        hapticFeedback()
        onNavigateToScanner()
    }
    
    TopBarActions(
        listOf(
            TopBarAction(Icons.Default.Share, Res.string.action_share) {
                hapticFeedback()
                sheets.show(ShareAppSheet)
            }
        )
    )
    
    if (isPortraitMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShareQrCode(
                modifier = Modifier.weight(1f),
                value = sharedData,
                serializer = SharedData.serializer()
            )
            
            CodeScannerInfo(
                sharedText = sharedText,
                useEncryptedShare = useEncryptedShare,
                supportsScanning = foundation.supports(PlatformFeature.CODE_SCANNING),
                onUpdateSharedText = onUpdateSharedText,
                onClickScanner = onClickScanner
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CodeScannerInfo(
                modifier = Modifier.weight(1f),
                sharedText = sharedText,
                useEncryptedShare = useEncryptedShare,
                supportsScanning = foundation.supports(PlatformFeature.CODE_SCANNING),
                onUpdateSharedText = onUpdateSharedText,
                onClickScanner = onClickScanner
            )
            
            ShareQrCode(
                modifier = Modifier.weight(1f),
                value = sharedData,
                serializer = SharedData.serializer()
            )
        }
    }
}

@Composable
fun CodeScannerInfo(
    modifier: Modifier = Modifier,
    sharedText: String? = null,
    useEncryptedShare: Boolean,
    supportsScanning: Boolean,
    onUpdateSharedText: (String) -> Unit,
    onClickScanner: () -> Unit,
) {
    val inputHint = if (useEncryptedShare) {
        stringResource(Res.string.generate_qr_code_encrypted_text)
    } else {
        stringResource(Res.string.generate_qr_code_text)
    }
    
    Column(
        modifier = modifier
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                TextInput(
                    modifier = Modifier
                        .padding(16.dp),
                    text = sharedText ?: "",
                    minLines = 3,
                    maxLines = 3,
                    hintText = inputHint
                ) { newText ->
                    onUpdateSharedText(newText)
                }
                
                Spacer(Modifier.height(6.dp))
            }
        }
        
        if (supportsScanning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 8.dp,
                    alignment = Alignment.CenterHorizontally
                )
            ) {
                FloatingButton(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(Res.string.scanner_button_text),
                    onClick = {
                        onClickScanner()
                    }
                )
            }
        }
    }
}