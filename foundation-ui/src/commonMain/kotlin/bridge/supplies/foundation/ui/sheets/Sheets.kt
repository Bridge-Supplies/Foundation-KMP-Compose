package bridge.supplies.foundation.ui.sheets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.sheets.SheetController
import bridge.supplies.foundation.core.sheets.SheetRequest
import bridge.supplies.foundation.core.time.getTodayUtcMs
import bridge.supplies.foundation.ui.components.CodeDisplay
import bridge.supplies.foundation.ui.components.DatePickerCard
import bridge.supplies.foundation.ui.components.EdgeFadeColumn
import bridge.supplies.foundation.ui.components.HintText
import bridge.supplies.foundation.ui.components.TextButton
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.isPortrait
import bridge.supplies.foundation.ui.rememberHapticFeedback
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.navigation_close
import bridge.supplies.foundation.ui.resources.navigation_confirm
import bridge.supplies.foundation.ui.resources.share_sheet_copy_text
import bridge.supplies.foundation.ui.util.setClipboardText
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Available to sheet content. */
public interface SheetScope {
    /** Animates the sheet away, then clears it. */
    public fun dismiss()

    /** Haptic feedback honoring the user's setting. */
    public fun hapticFeedback()
}

/** The app's sheet content for its own [SheetRequest]s. Foundation wraps it in a modal bottom sheet. */
public typealias SheetContent = @Composable SheetScope.(request: SheetRequest) -> Unit

@Composable
public fun sheetController(): SheetController = koinInject()


// Built-in sheets

/** A date picker sheet. Use [selectableDates] to restrict choices, e.g. `PastOrPresentSelectableDates`. */
@OptIn(ExperimentalMaterial3Api::class)
public class DateSheet(
    public val initialDateMs: Long = getTodayUtcMs(),
    public val selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    public val onSelected: (Long) -> Unit,
) : SheetRequest

/** A QR code and copy button for the app's [FoundationConfig.shareUrl][bridge.supplies.foundation.core.config.FoundationConfig.shareUrl]. */
public data object ShareAppSheet : SheetRequest


// Host

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SheetHost(sheets: SheetContent) {
    val controller = sheetController()
    val request by controller.current.collectAsState()
    val current = request ?: return

    // Keyed on orientation too: the sheet doesn't adapt to rotation otherwise
    key(current, isPortrait()) {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
        val coroutineScope = rememberCoroutineScope()
        val haptic = rememberHapticFeedback()
        val scope = remember(sheetState) {
            object : SheetScope {
                override fun dismiss() {
                    coroutineScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible && controller.current.value == current) {
                            controller.dismiss()
                        }
                    }
                }

                override fun hapticFeedback() = haptic()
            }
        }

        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            onDismissRequest = { controller.dismiss() }
        ) {
            when (current) {
                is DateSheet -> scope.DateSheetContent(current)
                is ShareAppSheet -> scope.ShareAppSheetContent()
                else -> scope.sheets(current)
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}


// Building blocks

/** Scrollable sheet body with Foundation's padding and edge fades. */
@Composable
public fun SheetColumn(content: @Composable ColumnScope.() -> Unit) {
    EdgeFadeColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = rememberScrollState(),
        fadeColor = MaterialTheme.colorScheme.surfaceContainerLow,
        content = content
    )
}

/** Outlined full-width close button. */
@Composable
public fun SheetScope.CloseButton(modifier: Modifier = Modifier.fillMaxWidth()) {
    TextButton(
        modifier = modifier,
        text = stringResource(Res.string.navigation_close),
        outlined = true
    ) {
        dismiss()
        hapticFeedback()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetScope.DateSheetContent(request: DateSheet) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = request.initialDateMs,
        initialDisplayMode = DisplayMode.Input,
        selectableDates = request.selectableDates
    )

    LaunchedEffect(Unit) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .filterNotNull()
            .drop(1) // drop first value
            .collect {
                hapticFeedback()
            }
    }

    SheetColumn {
        DatePickerCard(
            modifier = Modifier
                .padding(top = 8.dp),
            datePickerState = datePickerState
        )

        TextButton(
            text = stringResource(Res.string.navigation_confirm)
        ) {
            request.onSelected(datePickerState.selectedDateMillis ?: request.initialDateMs)
            dismiss()
            hapticFeedback()
        }

        CloseButton()
    }
}

@Composable
private fun SheetScope.ShareAppSheetContent() {
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val shareText = foundationViewModel().config.shareUrl.orEmpty()

    SheetColumn {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            CodeDisplay(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(if (isPortrait()) 256.dp else 200.dp),
                text = shareText,
                color = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.onPrimary,
                cardColor = MaterialTheme.colorScheme.onSurface
            )
        }

        HintText(
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = shareText
        )

        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            text = stringResource(Res.string.share_sheet_copy_text)
        ) {
            coroutineScope.launch {
                setClipboardText(clipboard, shareText)
            }
            dismiss()
            hapticFeedback()
        }

        CloseButton()
    }
}
