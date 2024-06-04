import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    
    private val _useDarkTheme = MutableStateFlow(Prefs.DARK_MODE.defaultValue as Int)
    var useDarkTheme = _useDarkTheme.asStateFlow()
        private set
    
    private val _useDynamicColors = MutableStateFlow(Prefs.DYNAMIC_COLORS.defaultValue as Boolean)
    var useDynamicColors = _useDynamicColors.asStateFlow()
        private set
    
    private val _useVibration = MutableStateFlow(Prefs.VIBRATION.defaultValue as Boolean)
    var useVibration = _useVibration.asStateFlow()
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
            repository.getDarkModeFlow().collectLatest {
                _useDarkTheme.value = it
            }
        }
        launch {
            repository.getDynamicColorsFlow().collectLatest {
                _useDynamicColors.value = it
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
    
    fun useDarkTheme(option: Int) = launch {
        repository.setDarkMode(option)
    }
    
    fun useDynamicColors(enabled: Boolean) = launch {
        repository.setDynamicColors(enabled)
    }
    
    fun useVibration(enabled: Boolean) = launch {
        repository.setVibration(enabled)
    }
    
    fun hapticFeedback(haptics: HapticFeedback) {
        if (useVibration.value && supportsFeature(Feature.VIBRATION)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}