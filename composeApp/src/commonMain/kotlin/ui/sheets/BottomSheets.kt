package ui.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import config.AppliedColorScheme
import config.PlatformType
import data.MainViewModel
import data.getTodayUtcMs
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.navigation_close
import foundation.composeapp.generated.resources.navigation_confirm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

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
    colorScheme: AppliedColorScheme,
    sheetState: SheetState = rememberModalBottomSheetState(true),
    closeSheet: () -> Unit,
    content: @Composable () -> Unit
) {
    val buttonColors = ButtonColors(
        containerColor = colorScheme.buttonColor,
        contentColor = colorScheme.onButtonColor,
        disabledContainerColor = colorScheme.buttonColor,
        disabledContentColor = colorScheme.onButtonColor
    )
    
    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = if (platform == PlatformType.IOS) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        },
        onDismissRequest = closeSheet,
        containerColor = colorScheme.cardColor,
        contentColor = colorScheme.onCardColor
    ) {
        Column(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 48.dp)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
            
            Button(
                onClick = closeSheet,
                colors = buttonColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp),
            ) {
                Text(stringResource(Res.string.navigation_close))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    colorScheme: AppliedColorScheme,
    selectedDate: Long = getTodayUtcMs(),
    onVibrate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        initialDisplayMode = if (viewModel.platform.type == PlatformType.DESKTOP)
            DisplayMode.Input else DisplayMode.Picker
    )
    
    LaunchedEffect(Unit) {
        snapshotFlow { datePickerState.selectedDateMillis }
            .filterNotNull()
            .drop(1) // drop first value
            .collect {
                onVibrate()
            }
    }
    
    val buttonColors = ButtonColors(
        containerColor = colorScheme.buttonColor,
        contentColor = colorScheme.onButtonColor,
        disabledContainerColor = colorScheme.buttonColor,
        disabledContentColor = colorScheme.onButtonColor
    )
    
    val closeSheet: () -> Unit = {
        coroutineScope.hideSheet(sheetState, viewModel)
        onVibrate()
    }
    
    SimpleBottomSheet(
        platform = viewModel.platform.type,
        colorScheme = colorScheme,
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
            colors = buttonColors,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp),
        ) {
            Text(stringResource(Res.string.navigation_confirm))
        }
    }
}