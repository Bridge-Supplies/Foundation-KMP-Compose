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
import ui.AppBarAction
import ui.sheets.ActiveBottomSheet

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
    
    
    // FOUNDATION
    
    private val _licenses = MutableStateFlow(LicenseMap())
    var licenses = _licenses.asStateFlow()
        private set
    
    private val _useEncryptedShare = MutableStateFlow(Prefs.ENCRYPTED_SHARE.defaultValue as Boolean)
    var useEncryptedShare = _useEncryptedShare.asStateFlow()
        private set
    
    private val _useLandingTips = MutableStateFlow(Prefs.LANDING_TIPS.defaultValue as Boolean)
    var useLandingTips = _useLandingTips.asStateFlow()
        private set
    
    private val _useFullscreenLandscape = MutableStateFlow(Prefs.FULLSCREEN_LANDSCAPE.defaultValue as Boolean)
    var useFullscreenLandscape = _useFullscreenLandscape.asStateFlow()
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
    
    private val _currentBottomSheet: MutableStateFlow<ActiveBottomSheet> = MutableStateFlow(ActiveBottomSheet.None)
    var currentBottomSheet = _currentBottomSheet.asStateFlow()
        private set
    
    private val _selectedDate = MutableStateFlow(getTodayUtcMs())
    var selectedDate = _selectedDate.asStateFlow()
        private set
    
    private val _sharedText = MutableStateFlow("")
    var sharedText = _sharedText.asStateFlow()
        private set
    
    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()
    
    private val _activeAppBarAction = MutableStateFlow<AppBarAction?>(null)
    var activeAppBarAction = _activeAppBarAction.asStateFlow()
        private set
    
    
    init {
        launchFlows()
        startTimer()
    }
    
    private fun launchFlows() {
        
        // FOUNDATION
        
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
        
        launch {
            repository.getLandingTipsFlow().collectLatest {
                _useLandingTips.value = it
            }
        }
        
        launch {
            repository.getEncryptedShareFlow().collectLatest {
                _useEncryptedShare.value = it
            }
        }
        
        launch {
            repository.getFullscreenLandscapeFlow().collectLatest {
                _useFullscreenLandscape.value = it
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
    
    
    // FOUNDATION
    
    fun useLandingTips(enabled: Boolean) {
        _useLandingTips.value = enabled
        launch {
            repository.setLandingTips(enabled)
        }
    }
    
    fun useEncryptedShare(enabled: Boolean) {
        _useEncryptedShare.value = enabled
        launch {
            repository.setEncryptedShare(enabled)
        }
    }
    
    fun useFullscreenLandscape(enabled: Boolean) {
        _useFullscreenLandscape.value = enabled
        launch {
            repository.setFullscreenLandscape(enabled)
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
    
    fun setSelectedDate(selectedDate: Long) {
        _selectedDate.value = selectedDate
    }
    
    fun setSharedText(text: String) {
        _sharedText.value = text
    }
    
    fun hapticFeedback(haptics: HapticFeedback) {
        if (useVibration.value && supportsFeature(Feature.VIBRATION)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    // BOTTOM SHEETS
    
    private fun setBottomSheet(sheet: ActiveBottomSheet) {
        _currentBottomSheet.value = sheet
    }
    
    fun hideBottomSheet() {
        _currentBottomSheet.value = ActiveBottomSheet.None
    }
    
    fun showDatePickerSheet(selectedDate: Long) {
        setBottomSheet(ActiveBottomSheet.DatePicker(selectedDate))
    }
    
    fun showShareSheet() {
        setBottomSheet(ActiveBottomSheet.ShareApp)
    }
    
    // APP BAR ACTIONS
    
    fun startAppBarAction(action: AppBarAction) {
        _activeAppBarAction.value = action
    }
    
    fun consumeAppBarAction() {
        _activeAppBarAction.value = null
    }
}