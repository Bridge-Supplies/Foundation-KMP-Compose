package screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.ColorSchemeStyle
import config.PlatformType
import config.getAppliedColorScheme
import config.isPortraitMode
import data.MainViewModel
import data.getDateDisplayString
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_date_elapsed_seconds
import foundation.composeapp.generated.resources.app_date_selected
import foundation.composeapp.generated.resources.app_date_today
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDateScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val colorScheme = getAppliedColorScheme(ColorSchemeStyle.PRIMARY)
    val isPortraitMode = isPortraitMode()
    val timer by viewModel.timer.collectAsState()
    val now = Clock.System.now()
    val todaysDate by remember(now) { mutableStateOf(getDateDisplayString(now)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = now.toEpochMilliseconds(),
            initialDisplayMode = if (viewModel.platform.type == PlatformType.DESKTOP) DisplayMode.Input else DisplayMode.Picker
        )
        
        val selectedDateDisplay = if (datePickerState.selectedDateMillis != null) {
            getDateDisplayString(
                now = Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!),
                zone = TimeZone.UTC
            )
        } else { "" }
        
        val text = stringResource(Res.string.app_date_elapsed_seconds, timer) + "\n" +
            stringResource(Res.string.app_date_today, todaysDate) + "\n" +
            stringResource(Res.string.app_date_selected, selectedDateDisplay)
        
        Text(
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
            color = colorScheme.onContentColor,
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        ElevatedCard(
            modifier = Modifier,
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorScheme.contentColor
            )
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}