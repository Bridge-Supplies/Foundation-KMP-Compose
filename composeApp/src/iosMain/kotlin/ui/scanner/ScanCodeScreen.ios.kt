package ui.scanner

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
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import ui.FloatingButton

class PermissionsViewModel(
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
actual fun CodeScanner(
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
        PermissionsViewModel(controller)
    }
    
    when (viewModel.state) {
        PermissionState.Granted -> {
            ScannerView(
                modifier = modifier,
                codeTypes = listOf(
                    BarcodeFormats.FORMAT_QR_CODE,
                ),
                showUi = false
            ) { result ->
                when (result) {
                    is BarcodeResult.OnSuccess -> {
                        println("Barcode: ${result.barcode.data}, format: ${result.barcode.format}")
                        onSuccess(result.barcode.data)
                    }
                    
                    is BarcodeResult.OnFailed -> {
                        println("Error: ${result.exception.message}")
                        onFailure(result.exception.message ?: "")
                    }
                    
                    BarcodeResult.OnCanceled -> {
                        onFailure("")
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
                    text = "Allow camera permission"
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
                    text = "Request camera permission"
                ) {
                    viewModel.provideOrRequestCameraPermission()
                }
            }
        }
    }
}