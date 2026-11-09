package bridge.supplies.foundation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
public fun SettingsSwitch(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    onEnabled(!enabled)
                }
                .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OptionDetailText(
            modifier =
                Modifier.weight(1f)
                    .padding(
                        vertical = 8.dp,
                        horizontal = 16.dp,
                    ),
            title = title,
            subtitle = subtitle,
        )

        Switch(
            checked = enabled,
            onCheckedChange = {
                onEnabled(it)
            },
            colors =
                SwitchDefaults.colors(uncheckedTrackColor = MaterialTheme.colorScheme.background),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun <T> SettingsSelector(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    optionList: List<T>,
    selectedOption: T,
    onSelectOption: (T) -> Unit,
    optionName: @Composable (T) -> String,
) {
    Column(modifier = modifier) {
        OptionDetailText(
            modifier =
                Modifier.padding(
                    vertical = 8.dp,
                    horizontal = 16.dp,
                ),
            title = title,
            subtitle = subtitle,
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 10.dp)
        ) {
            optionList.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == selectedOption,
                    onClick = {
                        onSelectOption(value)
                    },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = optionList.size,
                        ),
                ) {
                    Text(
                        text = optionName(value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
