package bridge.supplies.foundation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
public fun TextButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    outlined: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val buttonText =
        @Composable {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    if (outlined) {
        OutlinedButton(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick,
        ) {
            buttonText()
        }
    } else {
        Button(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick,
        ) {
            buttonText()
        }
    }
}

@Composable
public fun FloatingButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
public fun BoxScope.BottomButton(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.align(Alignment.BottomEnd).wrapContentHeight().padding(paddingValues),
        horizontalArrangement = Arrangement.End,
    ) {
        ExtendedFloatingActionButton(
            onClick = {
                onClick()
            }
        ) {
            Text(text = text)
        }
    }
}

@Composable
public fun BoxScope.BottomButtons(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit,
    iconButtons: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.align(Alignment.BottomEnd).wrapContentHeight().padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconButtons()

        ExtendedFloatingActionButton(
            onClick = {
                onClick()
            }
        ) {
            Text(text = text)
        }
    }
}

@Composable
public fun SmallFab(
    modifier: Modifier = Modifier,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    SmallFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        containerColor = containerColor,
    ) {
        icon()
    }
}

@Composable
public fun ClickableIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    isFilled: Boolean = false,
    onClick: () -> Unit,
) {
    if (isFilled) {
        FilledIconButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
            )
        }
    } else {
        IconButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
public fun ToggleableIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enabledVector: ImageVector,
    disabledVector: ImageVector,
    contentDescription: String,
    onToggle: (enabled: Boolean) -> Unit,
) {
    IconToggleButton(
        modifier = modifier,
        checked = enabled,
        onCheckedChange = onToggle,
    ) {
        Icon(
            imageVector = if (enabled) enabledVector else disabledVector,
            contentDescription = contentDescription,
        )
    }
}
