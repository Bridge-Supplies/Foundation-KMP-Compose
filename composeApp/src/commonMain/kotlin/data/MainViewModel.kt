package data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.ColorTheme
import config.DarkMode
import config.Feature
import config.Palette
import config.Platform
import config.PlatformType
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
    
    private val _processedQrText = MutableStateFlow<String?>(null)
    var processedQrText = _processedQrText.asStateFlow()
        private set
    
    private val _processedQrBitmap = MutableStateFlow<ImageBitmap?>(null)
    var processedQrBitmap = _processedQrBitmap.asStateFlow()
        private set
    
    
    // FOUNDATION
    
    private val _currentBottomSheet: MutableStateFlow<ActiveBottomSheet> = MutableStateFlow(ActiveBottomSheet.None)
    var currentBottomSheet = _currentBottomSheet.asStateFlow()
        private set
    
    private val _activeAppBarAction = MutableStateFlow<AppBarAction?>(null)
    var activeAppBarAction = _activeAppBarAction.asStateFlow()
        private set
    
    private val _licenses = MutableStateFlow(LicenseMap())
    var licenses = _licenses.asStateFlow()
        private set
    
    private val _desktopResolutionX = MutableStateFlow(StoredValue.DESKTOP_RESOLUTION_X().default)
    var desktopResolutionX = _desktopResolutionX.asStateFlow()
        private set
    
    private val _desktopResolutionY = MutableStateFlow(StoredValue.DESKTOP_RESOLUTION_Y().default)
    var desktopResolutionY = _desktopResolutionY.asStateFlow()
        private set
    
    private val _useLandingTips = MutableStateFlow(StoredValue.LANDING_TIPS().default)
    var useLandingTips = _useLandingTips.asStateFlow()
        private set
    
    private val _useFullscreenLandscape = MutableStateFlow(StoredValue.FULLSCREEN_LANDSCAPE().default)
    var useFullscreenLandscape = _useFullscreenLandscape.asStateFlow()
        private set
    
    private val _useColorTheme = MutableStateFlow(StoredValue.COLOR_THEME().default)
    var useColorTheme = _useColorTheme.asStateFlow()
        private set
    
    private val _usePalette = MutableStateFlow(StoredValue.PALETTE_STYLE().default)
    var usePalette = _usePalette.asStateFlow()
        private set
    
    private val _useDarkMode = MutableStateFlow(StoredValue.DARK_MODE().default)
    var useDarkMode = _useDarkMode.asStateFlow()
        private set
    
    private val _useVibration = MutableStateFlow(StoredValue.VIBRATION().default)
    var useVibration = _useVibration.asStateFlow()
        private set
    
    private val _useScrollHelpers = MutableStateFlow(StoredValue.SCROLL_HELPERS().default)
    var useScrollHelpers = _useScrollHelpers.asStateFlow()
        private set
    
    private val _useEncryptedShare = MutableStateFlow(StoredValue.ENCRYPTED_SHARE().default)
    var useEncryptedShare = _useEncryptedShare.asStateFlow()
        private set
    
    // SESSION
    
    private val _selectedDate = MutableStateFlow(getTodayUtcMs())
    var selectedDate = _selectedDate.asStateFlow()
        private set
    
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
        
        if (platform.type == PlatformType.DESKTOP) {
            launch {
                repository.getDesktopResolutionXFlow().collectLatest {
                    _desktopResolutionX.value = it
                }
            }
            
            launch {
                repository.getDesktopResolutionYFlow().collectLatest {
                    _desktopResolutionY.value = it
                }
            }
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
        
        if (platform.supportsFeature(Feature.FULLSCREEN_LANDSCAPE)) {
            launch {
                repository.getFullscreenLandscapeFlow().collectLatest {
                    _useFullscreenLandscape.value = it
                }
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
        
        if (platform.supportsFeature(Feature.VIBRATION)) {
            launch {
                repository.getVibrationFlow().collectLatest {
                    _useVibration.value = it
                }
            }
        }
        
        launch {
            repository.getScrollHelpersFlow().collectLatest {
                _useScrollHelpers.value = it
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
    
    fun setSelectedDate(selectedDate: Long) {
        _selectedDate.value = selectedDate
    }
    
    fun setSharedText(text: String) {
        _sharedText.value = text
    }
    
    fun supportsFeature(feature: Feature) =
        platform.supportsFeature(feature)
    
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
    
    
    // FOUNDATION
    
    fun setDesktopResolutionX(x: Int) {
        _desktopResolutionX.value = x
        launch {
            repository.setDesktopResolutionX(x)
        }
    }
    
    fun setDesktopResolutionY(y: Int) {
        _desktopResolutionY.value = y
        launch {
            repository.setDesktopResolutionY(y)
        }
    }
    
    fun useLandingTips(enabled: Boolean) {
        _useLandingTips.value = enabled
        launch {
            repository.setLandingTips(enabled)
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
        clearQr()
        launch {
            repository.setColorTheme(option)
        }
    }
    
    fun usePalette(option: Palette) {
        _usePalette.value = option
        clearQr()
        launch {
            repository.setPaletteStyle(option)
        }
    }
    
    fun useDarkMode(option: DarkMode) {
        _useDarkMode.value = option
        clearQr()
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
    
    fun hapticFeedback(haptics: HapticFeedback) {
        if (useVibration.value && supportsFeature(Feature.VIBRATION)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    fun useScrollHelpers(enabled: Boolean) {
        _useScrollHelpers.value = enabled
        launch {
            repository.setScrollHelpers(enabled)
        }
    }
    
    fun useEncryptedShare(enabled: Boolean) {
        _useEncryptedShare.value = enabled
        clearQr()
        launch {
            repository.setEncryptedShare(enabled)
        }
    }
}