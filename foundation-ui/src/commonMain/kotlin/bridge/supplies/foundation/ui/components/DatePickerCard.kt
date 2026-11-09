package bridge.supplies.foundation.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.core.time.getDateDisplay
import bridge.supplies.foundation.core.time.getDateTime
import bridge.supplies.foundation.core.time.getTodayDate
import bridge.supplies.foundation.core.time.getTodayUtcMs
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.invalid_date
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
public object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= getTodayUtcMs()

    override fun isSelectableYear(year: Int): Boolean = year <= getTodayDate().year
}

/**
 * A date picker in a card. [title] replaces the headline text (default: the selected date, e.g.
 * "Birthday: …"), and [trailing] adds content after it, e.g. an icon for the selected date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun DatePickerCard(
    modifier: Modifier = Modifier,
    datePickerState: DatePickerState,
    title: @Composable (selectedDateMs: Long?) -> String = { dateMs ->
        if (dateMs != null) {
            getDateDisplay(getDateTime(dateMs))
        } else {
            stringResource(Res.string.invalid_date)
        }
    },
    trailing: (@Composable (selectedDateMs: Long?) -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        DatePicker(
            modifier = Modifier.padding(top = 8.dp),
            state = datePickerState,
            title = null,
            headline = {
                val dateMs = datePickerState.selectedDateMillis

                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title(dateMs),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp).animateContentSize(),
                    )

                    if (trailing != null) {
                        Spacer(Modifier.weight(1f))
                        trailing(dateMs)
                    }
                }
            },
        )
    }
}
