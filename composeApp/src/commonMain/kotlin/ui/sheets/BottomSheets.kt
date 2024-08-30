package ui.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import config.PlatformType
import config.getScreenSizeInfo
import config.isPortraitMode
import data.MainViewModel
import data.getTodayUtcMs
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.navigation_close
import foundation.composeapp.generated.resources.navigation_confirm
import foundation.composeapp.generated.resources.share_button_text
import foundation.composeapp.generated.resources.share_sheet_copy_text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ui.PastOrPresentSelectableDates
import ui.scanner.CodeDisplay

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
    platform: PlatformType,
    sheetState: SheetState = rememberModalBottomSheetState(true),
    closeSheet: () -> Unit,
    content: @Composable ColumnScope.(modifier: Modifier) -> Unit
) {
    val bottomSheet = @Composable {
        ModalBottomSheet(
            sheetState = sheetState,
            dragHandle = if (platform == PlatformType.IOS || platform == PlatformType.ANDROID) {
                { BottomSheetDefaults.DragHandle() }
            } else {
                null
            },
            onDismissRequest = closeSheet
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 48.dp)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sheetMaxContentHeight = getScreenSizeInfo().dpHeight / 2f
                content(Modifier.heightIn(max = sheetMaxContentHeight))
                
                OutlinedButton(
                    onClick = closeSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(Res.string.navigation_close))
                }
            }
        }
    }
    
    // workaround for bottom sheets not adapting to orientation changes
    if (isPortraitMode()) {
        bottomSheet()
    } else {
        bottomSheet()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    selectedDate: Long = getTodayUtcMs(),
    onVibrate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
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
                onVibrate()
            }
    }
    
    val closeSheet: () -> Unit = {
        coroutineScope.hideSheet(sheetState, viewModel)
        onVibrate()
    }
    
    SimpleBottomSheet(
        platform = viewModel.platform.type,
        sheetState = sheetState,
        closeSheet = closeSheet
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier
                .padding(8.dp)
        )
        
        Button(
            onClick = {
                viewModel.setSelectedDate(datePickerState.selectedDateMillis ?: selectedDate)
                closeSheet()
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 8.dp),
        ) {
            Text(stringResource(Res.string.navigation_confirm))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppBottomSheet(
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    onVibrate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val clipboardManager = LocalClipboardManager.current
    
    val closeSheet: () -> Unit = {
        coroutineScope.hideSheet(sheetState, viewModel)
        onVibrate()
    }
    
    SimpleBottomSheet(
        platform = viewModel.platform.type,
        sheetState = sheetState,
        closeSheet = closeSheet
    ) {
        Text(
            text = stringResource(Res.string.share_button_text),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        CodeDisplay(
            modifier = Modifier
                .wrapContentWidth()
                .height(if (isPortraitMode()) 256.dp else 200.dp),
            text = viewModel.platform.shareUrl,
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.onPrimary,
            cardColor = MaterialTheme.colorScheme.onSurface
        )
        
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(viewModel.platform.shareUrl))
                closeSheet()
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp),
        ) {
            Text(stringResource(Res.string.share_sheet_copy_text))
        }
    }
}