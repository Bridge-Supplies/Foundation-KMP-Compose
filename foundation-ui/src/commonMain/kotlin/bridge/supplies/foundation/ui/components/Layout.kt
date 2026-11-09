package bridge.supplies.foundation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
public fun Modifier.consumeClick(): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
    ) {
        /* no-op */
    }

@Composable
public fun StickyHeader(
    modifier: Modifier = Modifier,
    titleText: String,
    subtitleText: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    iconButtons: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().consumeClick()) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .background(backgroundColor)
                    .then(modifier) // apply inner padding etc afterwards
                    .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(8.dp))

            OptionDetailText(
                modifier =
                    Modifier.weight(1f)
                        .padding(vertical = if (subtitleText == null) 8.dp else 0.dp)
                        .padding(end = 8.dp)
                        .semantics { heading() },
                title = titleText,
                subtitle = subtitleText,
                subtitleMaxLines = 1,
            )

            iconButtons()
        }

        EdgeFadeWrapper(
            showStartEdgeFade = true,
            showEndEdgeFade = false,
            orientation = Orientation.Vertical,
            fadeSize = 8.dp,
            fadeColor = backgroundColor,
        ) {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
public fun HorizontalSeparator() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
    )
}

@Composable
public fun HorizontalSpacer(height: Dp) {
    Spacer(
        Modifier.fillMaxWidth()
            .height(
                height.coerceAtLeast(0.5.dp)
            ) // needed for edge fades to detect start/end of lists
    )
}

@Composable
public fun VerticalSpacer(width: Dp) {
    Spacer(
        Modifier.fillMaxHeight()
            .width(
                width.coerceAtLeast(0.5.dp)
            ) // needed for edge fades to detect start/end of lists
    )
}

public suspend fun showSnackBar(
    message: String,
    actionLabel: String,
    snackbarHost: SnackbarHostState,
    hapticFeedback: () -> Unit,
    onComplete: (() -> Unit)? = null,
) {
    // clear any existing snackbars
    snackbarHost.currentSnackbarData?.dismiss()

    hapticFeedback()

    val snackbar =
        snackbarHost.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
        )

    if (snackbar == SnackbarResult.ActionPerformed) {
        snackbarHost.currentSnackbarData?.dismiss()
        onComplete?.invoke()
    }
}

@Composable
public fun <T> rememberInteractionSources(items: List<T>): Map<T, MutableInteractionSource> {
    return remember {
        mutableMapOf<T, MutableInteractionSource>().apply {
            items.forEach { item ->
                this[item] = MutableInteractionSource()
            }
        }
    }
}

public suspend fun attentionRipple(
    interactionSource: MutableInteractionSource,
    duration: Long = 1000L,
) {
    val press = PressInteraction.Press(Offset.Infinite)
    interactionSource.emit(press)
    delay(duration)
    interactionSource.emit(PressInteraction.Release(press))
}
