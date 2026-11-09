package bridge.supplies.foundation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.ui.resources.Res
import bridge.supplies.foundation.ui.resources.button_expand_less
import bridge.supplies.foundation.ui.resources.button_expand_more
import bridge.supplies.foundation.ui.shell.TRANSITION_EASING
import bridge.supplies.foundation.ui.shell.TRANSITION_ENTER_MS
import org.jetbrains.compose.resources.stringResource

@Composable
public fun TitledCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = false,
    isElevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    iconButtons: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardContent =
        @Composable {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        TitleText(
                            text = title,
                            modifier = Modifier.padding(top = 6.dp).padding(horizontal = 16.dp),
                        )

                        if (subtitle != null) {
                            SubtitleText(
                                text = subtitle,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    iconButtons()

                    Spacer(Modifier.width(8.dp))
                }

                Spacer(Modifier.height(10.dp))

                if (showDivider) {
                    HorizontalSeparator()
                }

                content()
            }
        }

    when {
        isElevated && onClick != null -> {
            ElevatedCard(
                modifier = modifier,
                onClick = onClick,
            ) {
                cardContent()
            }
        }

        isElevated -> {
            ElevatedCard(modifier = modifier) {
                cardContent()
            }
        }

        !isElevated && onClick != null -> {
            Card(
                modifier = modifier,
                onClick = onClick,
            ) {
                cardContent()
            }
        }

        else -> {
            Card(modifier = modifier) {
                cardContent()
            }
        }
    }
}

@Composable
public fun ExpandableTitledCard(
    modifier: Modifier = Modifier,
    title: String,
    showDivider: Boolean = false,
    isElevated: Boolean = false,
    maxUnexpandedHeight: Dp = 172.dp,
    isExpanded: Boolean,
    onExpand: ((expanded: Boolean) -> Unit)? = null,
    content: @Composable ColumnScope.(isExpanded: Boolean) -> Unit,
) {
    val animatedIcon by
        animateFloatAsState(
            targetValue = if (isExpanded) 180f else 360f,
            animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING),
        )

    val animatedMaxHeight by
        animateDpAsState(
            label = "max_height",
            targetValue =
                if (isExpanded) {
                    Float.POSITIVE_INFINITY.dp
                } else {
                    maxUnexpandedHeight
                },
        )

    val onClick: () -> Unit = {
        onExpand?.invoke(!isExpanded)
    }

    val cardContent =
        @Composable {
            EdgeFadeWrapper(
                showStartEdgeFade = false,
                showEndEdgeFade = !isExpanded,
                orientation = Orientation.Vertical,
            ) {
                Column(
                    modifier =
                        Modifier.animateContentSize()
                            .heightIn(max = animatedMaxHeight)
                            .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TitleText(
                            modifier =
                                Modifier.weight(1f).padding(end = 16.dp).padding(bottom = 4.dp),
                            text = title,
                            maxLines = if (isExpanded) 2 else 1,
                        )

                        ClickableIcon(
                            modifier = Modifier.rotate(animatedIcon).size(44.dp),
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription =
                                if (isExpanded) {
                                    stringResource(Res.string.button_expand_less)
                                } else {
                                    stringResource(Res.string.button_expand_more)
                                },
                        ) {
                            onClick()
                        }
                    }

                    if (showDivider) {
                        HorizontalSeparator()
                    }

                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        content(isExpanded)

                        AnimatedVisibility(isExpanded) {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

    if (isElevated) {
        ElevatedCard(
            modifier = modifier,
            onClick = {
                onClick()
            },
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier,
            onClick = {
                onClick()
            },
        ) {
            cardContent()
        }
    }
}
