package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Demo state. Foundation's settings live in FoundationViewModel. */
class MainViewModel : ViewModel() {
    private fun launch(action: suspend () -> Unit) {
        // define Default to address Desktop build issue of missing Main thread
        viewModelScope.launch(Dispatchers.Default) {
            action()
        }
    }
    
    private val _processedQrText = MutableStateFlow<String?>(null)
    var processedQrText = _processedQrText.asStateFlow()
        private set
    
    private val _processedQrBitmap = MutableStateFlow<ImageBitmap?>(null)
    var processedQrBitmap = _processedQrBitmap.asStateFlow()
        private set
    
    
    // FOUNDATION
    
    private val _licenses = MutableStateFlow(LicenseMap())
    var licenses = _licenses.asStateFlow()
        private set
    
    // SESSION
    
    private val _sharedText = MutableStateFlow("")
    var sharedText = _sharedText.asStateFlow()
        private set
    
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()
    
    
    init {
        launchFlows()
        startTimer()
    }
    
    private fun launchFlows() {
        launch {
            _licenses.value = LicenseMap(
                loadLicenseFile().licenses
                    .sortedBy {
                        it.moduleName
                    }
                    .groupBy {
                        it.moduleLicenseUrl
                            .replaceFirst(oldValue = "https://", newValue = "")
                            .replaceFirst(oldValue = "http://", newValue = "")
                    }
            )
        }
    }
    
    // UTILITIES
    
    private fun startTimer() {
        launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }
    
    fun setSharedText(text: String) {
        _sharedText.value = text
    }
    
    fun setQr(
        processedText: String?,
        qr: ImageBitmap?
    ) {
        _processedQrText.value = processedText
        _processedQrBitmap.value = qr
    }
    
    fun clearQr() {
        _processedQrText.value = null
        _processedQrBitmap.value = null
    }
}
