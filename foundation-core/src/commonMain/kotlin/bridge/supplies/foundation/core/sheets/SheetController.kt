package bridge.supplies.foundation.core.sheets

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A bottom sheet to show. Apps define their own (usually a sealed class); Foundation has built-ins in foundation-ui. */
public interface SheetRequest

/** Shows one bottom sheet at a time. Injectable, so ViewModels can open sheets too. */
public interface SheetController {
    public val current: StateFlow<SheetRequest?>
    
    /** Shows [request], replacing any sheet already showing. */
    public fun show(request: SheetRequest)
    
    public fun dismiss()
}

public class DefaultSheetController : SheetController {
    private val _current = MutableStateFlow<SheetRequest?>(null)
    override val current: StateFlow<SheetRequest?> = _current.asStateFlow()
    
    override fun show(request: SheetRequest) {
        _current.value = request
    }
    
    override fun dismiss() {
        _current.value = null
    }
}
