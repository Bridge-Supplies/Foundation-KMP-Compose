package data

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    val platform: Platform,
    private val repository: DataRepository
) : ViewModel() {
    
    private fun launch(action: suspend () -> Unit) {
        // define Default to address Desktop build issue of missing Main thread
        viewModelScope.launch(Dispatchers.Default) {
            action()
        }
    }
    
    
    // SETTINGS
    
    private val _useEncryptedShare = MutableStateFlow(Prefs.ENCRYPTED_SHARE.defaultValue as Boolean)
    var useEncryptedShare = _useEncryptedShare.asStateFlow()
        private set
    
    private val _useColorTheme = MutableStateFlow(Prefs.COLOR_THEME.defaultValue as ColorTheme)
    var useColorTheme = _useColorTheme.asStateFlow()
        private set
    
    private val _usePalette = MutableStateFlow(Prefs.PALETTE_STYLE.defaultValue as Palette)
    var usePalette = _usePalette.asStateFlow()
        private set
    
    private val _useDarkMode = MutableStateFlow(Prefs.DARK_MODE.defaultValue as DarkMode)
    var useDarkMode = _useDarkMode.asStateFlow()
        private set
    
    private val _useVibration = MutableStateFlow(Prefs.VIBRATION.defaultValue as Boolean)
    var useVibration = _useVibration.asStateFlow()
        private set
    
    // SESSION
    
    private val _sharedText = MutableStateFlow("")
    var sharedText = _sharedText.asStateFlow()
        private set
    
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()
    
    
    // SETUP
    
    init {
        launchFlows()
        startTimer()
    }
    
    private fun launchFlows() {
        launch {
            repository.getEncryptedShareFlow().collectLatest {
                _useEncryptedShare.value = it
            }
        }
        
        launch {
            repository.getColorThemeFlow().collectLatest {
                _useColorTheme.value = it
            }
        }
        
        launch {
            repository.getPaletteStyleFlow().collectLatest {
                _usePalette.value = it
            }
        }
        
        launch {
            repository.getDarkModeFlow().collectLatest {
                _useDarkMode.value = it
            }
        }
        
        launch {
            repository.getVibrationFlow().collectLatest {
                _useVibration.value = it
            }
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
    
    fun supportsFeature(feature: Feature) =
        platform.supportsFeature(feature)
    
    fun useEncryptedShare(enabled: Boolean) {
        _useEncryptedShare.value = enabled
        launch {
            repository.setEncryptedShare(enabled)
        }
    }
    
    fun useColorTheme(option: ColorTheme) {
        _useColorTheme.value = option
        launch {
            repository.setColorTheme(option)
        }
    }
    
    fun usePalette(option: Palette) {
        _usePalette.value = option
        launch {
            repository.setPaletteStyle(option)
        }
    }
    
    fun useDarkMode(option: DarkMode) {
        _useDarkMode.value = option
        launch {
            repository.setDarkMode(option)
        }
    }
    
    fun useVibration(enabled: Boolean) {
        _useVibration.value = enabled
        launch {
            repository.setVibration(enabled)
        }
    }
    
    fun setSharedText(text: String) {
        _sharedText.value = text
    }
    
    fun hapticFeedback(haptics: HapticFeedback) {
        if (useVibration.value && supportsFeature(Feature.VIBRATION)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}