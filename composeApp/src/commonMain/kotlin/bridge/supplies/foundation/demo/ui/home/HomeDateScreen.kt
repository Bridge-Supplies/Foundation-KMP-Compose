package bridge.supplies.foundation.demo.ui.home

import bridge.supplies.foundation.ui.components.PastOrPresentSelectableDates
import bridge.supplies.foundation.ui.sheets.DateSheet
import bridge.supplies.foundation.ui.sheets.sheetController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.demo.data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_date_elapsed_seconds
import foundation.composeapp.generated.resources.app_date_selected
import foundation.composeapp.generated.resources.app_date_today
import foundation.composeapp.generated.resources.app_days_between
import foundation.composeapp.generated.resources.navigation_home_date_select
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import bridge.supplies.foundation.core.time.daysBetween
import bridge.supplies.foundation.core.time.getDateDisplay
import bridge.supplies.foundation.core.time.getDateTime
import bridge.supplies.foundation.core.time.getTodayDate
import bridge.supplies.foundation.core.time.getTodayUtcMs
import bridge.supplies.foundation.ui.components.FloatingButton
import bridge.supplies.foundation.ui.components.TitleText

@OptIn(ExperimentalTime::class)
@Composable
fun HomeDateScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit,
    initialSelectedDateMs: Long = getTodayUtcMs()
) {
    val sheets = sheetController()
    val timer by viewModel.timer.collectAsState()
    val now = getTodayDate()
    val todaysDate by remember { mutableStateOf(getDateDisplay(now)) }
    var selectedDate by remember { mutableStateOf(initialSelectedDateMs) }
    val selectedDateDisplay by remember(selectedDate) {
        derivedStateOf {
            getDateDisplay(getDateTime(selectedDate))
        }
    }
    val daysBetween by remember(selectedDate, todaysDate) {
        derivedStateOf {
            daysBetween(selectedDate, getTodayUtcMs())
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val text = stringResource(Res.string.app_date_elapsed_seconds, timer) + "\n" +
            stringResource(Res.string.app_date_today, todaysDate) + "\n" +
            stringResource(Res.string.app_date_selected, selectedDateDisplay) + "\n" +
            stringResource(Res.string.app_days_between, daysBetween)
        
        TitleText(
            textAlign = TextAlign.Start,
            text = text,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        Spacer(Modifier.weight(1f))
        
        FloatingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = stringResource(Res.string.navigation_home_date_select)
        ) {
            sheets.show(
                DateSheet(
                    initialDateMs = initialSelectedDateMs,
                    selectableDates = PastOrPresentSelectableDates,
                    onSelected = {
                        selectedDate = it
                    }
                )
            )
            hapticFeedback()
        }
    }
}