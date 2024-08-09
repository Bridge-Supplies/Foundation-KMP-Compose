package ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import config.isPortraitMode
import data.MainViewModel
import data.getDateDisplay
import data.getTodayDate
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_date_elapsed_seconds
import foundation.composeapp.generated.resources.app_date_selected
import foundation.composeapp.generated.resources.app_date_today
import foundation.composeapp.generated.resources.navigation_home_date_select
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeDateScreen(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val isPortraitMode = isPortraitMode()
    val timer by viewModel.timer.collectAsState()
    val now = getTodayDate()
    val todaysDate by remember(now) { mutableStateOf(getDateDisplay(now)) }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateDisplay by remember(selectedDate) {
        derivedStateOf {
            getDateDisplay(
                Instant.fromEpochMilliseconds(selectedDate).toLocalDateTime(TimeZone.UTC).date
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .verticalScroll(rememberScrollState())
            .padding(
                vertical = 16.dp,
                horizontal = if (isPortraitMode) 16.dp else viewModel.platform.landscapeContentPadding
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        val text = stringResource(Res.string.app_date_elapsed_seconds, timer) + "\n" +
            stringResource(Res.string.app_date_today, todaysDate) + "\n" +
            stringResource(Res.string.app_date_selected, selectedDateDisplay)
        
        Text(
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Spacer(Modifier.weight(1f))
        
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = {
                viewModel.showDatePickerSheet(selectedDate)
                onVibrate()
            }
        ) {
            Text(stringResource(Res.string.navigation_home_date_select))
        }
    }
}