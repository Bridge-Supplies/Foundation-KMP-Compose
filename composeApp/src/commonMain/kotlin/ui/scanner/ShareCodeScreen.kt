package ui.scanner

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import config.Feature
import config.isPortraitMode
import data.MainViewModel
import data.SharedData
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.generate_qr_code_encrypted_text
import foundation.composeapp.generated.resources.generate_qr_code_text
import foundation.composeapp.generated.resources.scanner_button_text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ui.AppBarAction
import ui.FloatingButton
import ui.QrCodeCard
import ui.Screen
import ui.TextInput
import ui.generateQrCode

@OptIn(FlowPreview::class)
@Composable
fun ShareCodeScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val coroutineScope = rememberCoroutineScope()
    val appBarAction by viewModel.activeAppBarAction.collectAsState()
    val sharedText by viewModel.sharedText.collectAsState()
    val useEncryptedShare by viewModel.useEncryptedShare.collectAsState()
    val qrProcessedText by viewModel.processedQrText.collectAsState()
    val qrBitmap by viewModel.processedQrBitmap.collectAsState()
    
    val color = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.onPrimary
    
    LaunchedEffect(sharedText, useEncryptedShare, color, backgroundColor) {
        coroutineScope.launch(Dispatchers.Default) {
            if (sharedText.isBlank()) {
                viewModel.clearQr()
            } else {
                val newProcessedText = SharedData.prepare(sharedText, useEncryptedShare)
                if (newProcessedText != null && newProcessedText != qrProcessedText) {
                    
                    val generatedQrBitmap = generateQrCode(
                        text = newProcessedText,
                        colorRgb = color.toArgb(),
                        backgroundColorRgb = backgroundColor.toArgb()
                    )
                    
                    viewModel.setQr(newProcessedText, generatedQrBitmap)
                }
            }
        }
    }
    
    val onUpdateSharedText = { newText: String ->
        viewModel.setSharedText(newText)
    }
    
    val onClickScanner = {
        hapticFeedback()
        onNavigateToScanner()
    }
    
    Screen.SHARE_GENERATE.actions.forEach { action ->
        if (appBarAction == action) {
            when (action) {
                AppBarAction.SHARE -> {
                    hapticFeedback()
                    viewModel.showShareSheet()
                    viewModel.consumeAppBarAction()
                }
                
                else -> {}
            }
        }
    }
    
    if (isPortraitMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoadingCodeDisplay(
                modifier = Modifier.weight(1f),
                sharedText = sharedText,
                qrBitmap = qrBitmap
            )
            
            CodeScannerInfo(
                sharedText = sharedText,
                useEncryptedShare = useEncryptedShare,
                supportsScanning = viewModel.supportsFeature(Feature.CODE_SCANNING),
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
                supportsScanning = viewModel.supportsFeature(Feature.CODE_SCANNING),
                onUpdateSharedText = onUpdateSharedText,
                onClickScanner = onClickScanner
            )
            
            LoadingCodeDisplay(
                modifier = Modifier.weight(1f),
                sharedText = sharedText,
                qrBitmap = qrBitmap
            )
        }
    }
}

@Composable
fun LoadingCodeDisplay(
    modifier: Modifier = Modifier,
    sharedText: String? = null,
    qrBitmap: ImageBitmap? = null
) {
    Crossfade(
        modifier = modifier,
        targetState = qrBitmap
    ) { qr ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (qr != null) {
                QrCodeCard(
                    qrBitmap = qr,
                    cardColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                if (!sharedText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
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