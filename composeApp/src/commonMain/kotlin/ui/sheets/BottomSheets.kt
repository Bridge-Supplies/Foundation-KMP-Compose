package ui.sheets

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
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.MainViewModel
import data.getTodayUtcMs
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.navigation_close
import foundation.composeapp.generated.resources.navigation_confirm
import foundation.composeapp.generated.resources.share_sheet_copy_text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ui.CodeDisplay
import ui.DatePickerCard
import ui.EdgeFadeColumn
import ui.HintText
import ui.PastOrPresentSelectableDates
import ui.TextButton

@OptIn(ExperimentalMaterial3Api::class)
fun CoroutineScope.hideSheet(sheetState: SheetState, viewModel: MainViewModel) {
    launch {
        sheetState.hide()
    }.invokeOnCompletion {
        if (!sheetState.isVisible) {
            viewModel.hideBottomSheet()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(true),
    closeSheet: () -> Unit,
    content: @Composable ColumnScope.(modifier: Modifier) -> Unit
) {
    val bottomSheet = @Composable {
        ModalBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            onDismissRequest = closeSheet
        ) {
            content(Modifier.padding(horizontal = 16.dp))
            
            Spacer(Modifier.navigationBarsPadding())
        }
    }
    
    // workaround for bottom sheets not adapting to orientation changes
    if (isPortraitMode()) {
        bottomSheet()
    } else {
        bottomSheet()
    }
}

@Composable
fun CloseButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    closeSheet: () -> Unit
) {
    TextButton(
        modifier = modifier,
        text = stringResource(Res.string.navigation_close),
        outlined = true
    ) {
        closeSheet()
    }
}


// BOTTOM SHEETS


// BOTTOM SHEETS

sealed class ActiveBottomSheet {
    data object None : ActiveBottomSheet()
    
    data object ShareApp : ActiveBottomSheet()
    
    data class DatePicker(
        var selectedDate: Long
    ) : ActiveBottomSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppBottomSheet(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val shareText = viewModel.platform.shareUrl
    
    val closeSheet: () -> Unit = {
        coroutineScope.hideSheet(sheetState, viewModel)
        hapticFeedback()
    }
    
    SimpleBottomSheet(
        sheetState = sheetState,
        closeSheet = closeSheet
    ) { modifier ->
        EdgeFadeColumn(
            modifier = modifier,
            state = scrollState,
            fadeColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CodeDisplay(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(if (isPortraitMode()) 256.dp else 200.dp),
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
                clipboardManager.setText(AnnotatedString(shareText))
                closeSheet()
            }
            
            CloseButton(
                closeSheet = closeSheet
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    viewModel: MainViewModel,
    selectedDate: Long = getTodayUtcMs(),
    hapticFeedback: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        initialDisplayMode = DisplayMode.Input,
        selectableDates = PastOrPresentSelectableDates
    )
    
    LaunchedEffect(Unit) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .filterNotNull()
            .drop(1) // drop first value
            .collect {
                hapticFeedback()
            }
    }
    
    val closeSheet: () -> Unit = {
        coroutineScope.hideSheet(sheetState, viewModel)
        hapticFeedback()
    }
    
    SimpleBottomSheet(
        sheetState = sheetState,
        closeSheet = closeSheet
    ) { modifier ->
        EdgeFadeColumn(
            modifier = modifier,
            state = scrollState,
            fadeColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            DatePickerCard(
                modifier = Modifier
                    .padding(top = 8.dp),
                datePickerState = datePickerState
            )
            
            TextButton(
                text = stringResource(Res.string.navigation_confirm)
            ) {
                viewModel.setSelectedDate(datePickerState.selectedDateMillis ?: selectedDate)
                closeSheet()
            }
            
            CloseButton(
                closeSheet = closeSheet
            )
        }
    }
}