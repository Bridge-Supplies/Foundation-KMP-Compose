package ui

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import data.getDateDisplay
import data.getDateTime
import data.getTodayDate
import data.getTodayUtcMs
import data.hideAndClearFocus
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.button_expand_less
import foundation.composeapp.generated.resources.button_expand_more
import foundation.composeapp.generated.resources.invalid_date
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

// FOUNDATION

// MODIFIERS

@Composable
fun Modifier.consumeClick() = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null
) { /* no-op */ }


// BASE COMPONENTS

@Composable
fun TitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.titleLarge,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor
    )
}

@Composable
fun SubtitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor
    )
}

@Composable
fun OptionDetailText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    titleMaxLines: Int = 1,
    subtitleMaxLines: Int = 3,
    textColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier
    ) {
        SubtitleText(
            modifier = Modifier
                .fillMaxWidth(),
            text = title,
            maxLines = titleMaxLines,
            textColor = textColor
        )
        
        if (subtitle != null) {
            SmallText(
                text = subtitle,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = subtitleMaxLines,
                textColor = textColor
            )
        }
    }
}

@Composable
fun BodyText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        color = textColor,
        textAlign = textAlign,
        minLines = minLines,
        maxLines = maxLines
    )
}

@Composable
fun SmallText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor,
        minLines = minLines,
        maxLines = maxLines
    )
}

@Composable
fun HintText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    minLines: Int = 1,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

@Composable
fun TextInput(
    modifier: Modifier = Modifier,
    text: String,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxTextLength: Int? = null,
    hintText: String? = null,
    onValueChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    var noteText by remember(text) {
        mutableStateOf(text)
    }
    
    LaunchedEffect(noteText) {
        onValueChange(noteText)
    }
    
    val supportingText: @Composable (() -> Unit)? =
        if (maxTextLength != null) {
            { Text("${noteText.length}/$maxTextLength") }
        } else null
    
    OutlinedTextField(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        value = noteText,
        onValueChange = {
            noteText = if (maxTextLength != null) it.take(maxTextLength) else it
        },
        label = {
            if (hintText != null) {
                Text(hintText)
            }
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController.hideAndClearFocus(focusManager)
            }
        ),
        minLines = minLines,
        maxLines = maxLines,
        supportingText = supportingText
    )
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    outlined: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val buttonText = @Composable {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    
    if (outlined) {
        OutlinedButton(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick
        ) {
            buttonText()
        }
    } else {
        Button(
            modifier = modifier,
            enabled = enabled,
            onClick = onClick
        ) {
            buttonText()
        }
    }
}

@Composable
fun FloatingButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BoxScope.BottomButton(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .wrapContentHeight()
            .padding(paddingValues),
        horizontalArrangement = Arrangement.End
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
fun BoxScope.BottomButtons(
    modifier: Modifier = Modifier,
    text: String,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit,
    iconButtons: @Composable RowScope.() -> Unit = { },
) {
    Row(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .wrapContentHeight()
            .padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
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
fun SmallFab(
    modifier: Modifier = Modifier,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    SmallFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        containerColor = containerColor
    ) {
        icon()
    }
}

@Composable
fun ClickableIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    isFilled: Boolean = false,
    onClick: () -> Unit
) {
    if (isFilled) {
        FilledIconButton(
            modifier = modifier,
            onClick = onClick
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    } else {
        IconButton(
            modifier = modifier,
            onClick = onClick
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun ToggleableIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enabledVector: ImageVector,
    disabledVector: ImageVector,
    contentDescription: String,
    onToggle: (enabled: Boolean) -> Unit
) {
    IconToggleButton(
        modifier = modifier,
        checked = enabled,
        onCheckedChange = onToggle
    ) {
        Icon(
            imageVector = if (enabled) enabledVector else disabledVector,
            contentDescription = contentDescription
        )
    }
}


// COMPLEX COMPONENTS

@Composable
fun TitledCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = false,
    isElevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    iconButtons: @Composable RowScope.() -> Unit = { },
    content: @Composable ColumnScope.() -> Unit
) {
    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    TitleText(
                        text = title,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .padding(horizontal = 16.dp)
                    )
                    
                    if (subtitle != null) {
                        SubtitleText(
                            text = subtitle,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
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
                onClick = onClick
            ) {
                cardContent()
            }
        }
        
        isElevated -> {
            ElevatedCard(
                modifier = modifier
            ) {
                cardContent()
            }
        }
        
        !isElevated && onClick != null -> {
            Card(
                modifier = modifier,
                onClick = onClick
            ) {
                cardContent()
            }
        }
        
        else -> {
            Card(
                modifier = modifier
            ) {
                cardContent()
            }
        }
    }
}

@Composable
fun ExpandableTitledCard(
    modifier: Modifier = Modifier,
    title: String,
    showDivider: Boolean = false,
    isElevated: Boolean = false,
    maxUnexpandedHeight: Dp = 172.dp,
    isExpanded: Boolean,
    onExpand: ((expanded: Boolean) -> Unit)? = null,
    content: @Composable ColumnScope.(isExpanded: Boolean) -> Unit
) {
    val animatedIcon by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 360f,
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )
    
    val animatedMaxHeight by animateDpAsState(
        label = "max_height",
        targetValue = if (isExpanded) {
            Float.POSITIVE_INFINITY.dp
        } else {
            maxUnexpandedHeight
        }
    )
    
    val onClick: () -> Unit = {
        onExpand?.invoke(!isExpanded)
    }
    
    val cardContent = @Composable {
        EdgeFadeWrapper(
            showStartEdgeFade = false,
            showEndEdgeFade = !isExpanded,
            orientation = Orientation.Vertical
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .heightIn(max = animatedMaxHeight)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TitleText(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                            .padding(bottom = 4.dp),
                        text = title,
                        maxLines = if (isExpanded) 2 else 1
                    )
                    
                    ClickableIcon(
                        modifier = Modifier
                            .rotate(animatedIcon)
                            .size(44.dp),
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = if (isExpanded) {
                            stringResource(Res.string.button_expand_less)
                        } else {
                            stringResource(Res.string.button_expand_more)
                        }
                    ) {
                        onClick()
                    }
                }
                
                if (showDivider) {
                    HorizontalSeparator()
                }
                
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
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
            }
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier,
            onClick = {
                onClick()
            }
        ) {
            cardContent()
        }
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                onEnabled(!enabled)
            }
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OptionDetailText(
            modifier = Modifier
                .weight(1f)
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                ),
            title = title,
            subtitle = subtitle
        )
        
        Switch(
            checked = enabled,
            onCheckedChange = {
                onEnabled(it)
            },
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsSelector(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    optionList: List<T>,
    selectedOption: T,
    onSelectOption: (T) -> Unit,
    optionName: @Composable (T) -> String
) {
    Column(
        modifier = modifier
    ) {
        OptionDetailText(
            modifier = Modifier
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                ),
            title = title,
            subtitle = subtitle
        )
        
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 10.dp)
        ) {
            optionList.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = value == selectedOption,
                    onClick = {
                        onSelectOption(value)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = optionList.size
                    )
                ) {
                    Text(
                        text = optionName(value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StickyHeader(
    modifier: Modifier = Modifier,
    titleText: String,
    subtitleText: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    iconButtons: @Composable RowScope.() -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .consumeClick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .then(modifier) // apply inner padding etc afterwards
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(8.dp))
            
            OptionDetailText(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = if (subtitleText == null) 8.dp else 0.dp)
                    .padding(end = 8.dp),
                title = titleText,
                subtitle = subtitleText,
                subtitleMaxLines = 1
            )
            
            iconButtons()
        }
        
        EdgeFadeWrapper(
            showStartEdgeFade = true,
            showEndEdgeFade = false,
            orientation = Orientation.Vertical,
            fadeSize = 8.dp,
            fadeColor = backgroundColor
        ) {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun HorizontalSeparator() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

@Composable
fun VerticalSeparator() {
    VerticalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
    )
}

@Composable
fun HorizontalSpacer(
    height: Dp
) {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(height.coerceAtLeast(0.5.dp)) // needed for edge fades to detect start/end of lists
    )
}

@Composable
fun VerticalSpacer(
    width: Dp
) {
    Spacer(
        Modifier
            .fillMaxHeight()
            .width(width.coerceAtLeast(0.5.dp)) // needed for edge fades to detect start/end of lists
    )
}


// FUNCTIONS

suspend fun showSnackBar(
    message: String,
    actionLabel: String,
    snackbarHost: SnackbarHostState,
    hapticFeedback: () -> Unit,
    onComplete: (() -> Unit)? = null
) {
    // clear any existing snackbars
    snackbarHost.currentSnackbarData?.dismiss()
    
    hapticFeedback()
    
    val snackbar = snackbarHost.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Short
    )
    
    if (snackbar == SnackbarResult.ActionPerformed) {
        snackbarHost.currentSnackbarData?.dismiss()
        onComplete?.invoke()
    }
}


// Adapted for multiplatform from: https://dev.to/bmonjoie/jetpack-compose-reveal-effect-1fao

@Composable
fun CircularReveal(
    modifier: Modifier = Modifier,
    startDelayMs: Int,
    revealDurationMs: Int,
    onCompleted: (suspend () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    
    val animationProgress: State<Float> = animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(revealDurationMs, easing = TRANSITION_EASING),
        label = "circular_reveal"
    )
    
    LaunchedEffect(Unit) {
        delay(startDelayMs.toLong())
        isRevealed = true
        delay(revealDurationMs.toLong())
        onCompleted?.invoke()
    }
    
    Box(
        modifier = modifier
            .circularReveal(animationProgress.value)
    ) {
        content()
    }
}

fun Modifier.circularReveal(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    offset: Offset? = null
) = clip(CircularRevealShape(progress, offset))

private class CircularRevealShape(
    @param:FloatRange(from = 0.0, to = 1.0) private val progress: Float,
    private val offset: Offset? = null
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            Path().apply {
                val startingOffset = offset ?: Offset(size.width / 2f, size.height / 2f)
                // end result slightly larger than full screen
                val revealRadius = max(size.width, size.height) * progress * 0.7f
                
                addOval(
                    Rect(
                        topLeft = Offset(
                            x = startingOffset.x - revealRadius,
                            y = startingOffset.y - revealRadius
                        ),
                        bottomRight = Offset(
                            x = startingOffset.x + revealRadius,
                            y = startingOffset.y + revealRadius
                        )
                    )
                )
            }
        )
    }
}

@Composable
fun <T> rememberInteractionSources(items: List<T>): Map<T, MutableInteractionSource> {
    return remember {
        mutableMapOf<T, MutableInteractionSource>().apply {
            items.forEach { item ->
                this[item] = MutableInteractionSource()
            }
        }
    }
}

suspend fun attentionRipple(
    interactionSource: MutableInteractionSource,
    duration: Long = 1000L
) {
    val press = PressInteraction.Press(Offset.Infinite)
    interactionSource.emit(press)
    delay(duration)
    interactionSource.emit(PressInteraction.Release(press))
}

@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= getTodayUtcMs()
    override fun isSelectableYear(year: Int): Boolean = year <= getTodayDate().year
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerCard(
    modifier: Modifier = Modifier,
    datePickerState: DatePickerState
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        DatePicker(
            modifier = Modifier
                .padding(top = 8.dp),
            state = datePickerState,
            title = null,
            headline = {
                val dateMs = datePickerState.selectedDateMillis
                val dateText = if (dateMs != null) {
                    getDateDisplay(getDateTime(dateMs))
                } else {
                    stringResource(Res.string.invalid_date)
                }
                
                Row(
                    modifier = Modifier
                        .padding(start = 24.dp)
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .animateContentSize()
                    )
                }
            }
        )
    }
}