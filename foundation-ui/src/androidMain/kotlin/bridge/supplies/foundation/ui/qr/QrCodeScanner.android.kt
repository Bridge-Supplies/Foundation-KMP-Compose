package bridge.supplies.foundation.ui.qr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import bridge.supplies.foundation.ui.components.FloatingButton
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.scanner_allow_camera
import bridge.supplies.foundation.ui.resources.scanner_request_camera
import org.jetbrains.compose.resources.stringResource

internal class CameraPermissionViewModel(
    private val controller: PermissionsController
) : ViewModel() {
    
    var state by mutableStateOf(PermissionState.NotDetermined)
        private set
    
    init {
        checkPermissionState()
    }
    
    fun checkPermissionState() {
        viewModelScope.launch {
            state = controller.getPermissionState(Permission.CAMERA)
        }
    }
    
    fun provideOrRequestCameraPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.CAMERA)
                state = PermissionState.Granted
            } catch (e: DeniedAlwaysException) {
                state = PermissionState.DeniedAlways
            } catch (e: DeniedException) {
                state = PermissionState.Denied
            } catch (e: RequestCanceledException) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
public actual fun QrCodeScanner(
    modifier: Modifier,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
    onDenied: () -> Unit
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    
    BindEffect(controller)
    
    val viewModel = viewModel {
        CameraPermissionViewModel(controller)
    }
    
    when (viewModel.state) {
        PermissionState.Granted -> {
            ScannerView(
                modifier = modifier,
                codeTypes = listOf(
                    BarcodeFormat.FORMAT_QR_CODE,
                )
            ) { result ->
                when (result) {
                    is BarcodeResult.OnSuccess -> {
                        onSuccess(result.barcode.data)
                    }
                    
                    is BarcodeResult.OnFailed -> {
                        onFailure(result.exception.message ?: "")
                    }

                }
            }
        }
        
        PermissionState.DeniedAlways -> {
            Box(
                modifier = modifier
                    .fillMaxSize(),
            ) {
                FloatingButton(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.scanner_allow_camera)
                ) {
                    controller.openAppSettings()
                    onDenied()
                }
            }
        }
        
        else -> {
            LaunchedEffect(Unit) {
                viewModel.provideOrRequestCameraPermission()
            }
            
            Box(
                modifier = modifier
                    .fillMaxSize(),
            ) {
                FloatingButton(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.scanner_request_camera)
                ) {
                    viewModel.provideOrRequestCameraPermission()
                }
            }
        }
    }
}