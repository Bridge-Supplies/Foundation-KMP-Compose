package ui

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
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
import data.getTodayDate
import data.getTodayUtcMs
import data.hideAndClearFocus
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.action_settings
import foundation.composeapp.generated.resources.button_expand_less
import foundation.composeapp.generated.resources.button_expand_more
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max

enum class AppBarAction(
    val labelRes: StringResource,
    val icon: ImageVector
) {
    SETTINGS(
        Res.string.action_settings,
        Icons.Default.Settings
    )
}


// FOUNDATION

@Composable
fun Modifier.consumeClick() = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null
) { /* no-op */ }

@Composable
fun TitledCard(
    modifier: Modifier = Modifier,
    title: String,
    isElevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            TitleText(
                text = title,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 10.dp)
                    .padding(horizontal = 16.dp)
            )
            
            HorizontalSeparator()
            
            content()
        }
    }
    
    if (isElevated) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            cardContent()
        }
    }
}

@Composable
fun ExpandableTitledCard(
    modifier: Modifier = Modifier,
    title: String,
    maxUnexpandedHeight: Dp = 172.dp,
    onExpand: ((expanded: Boolean) -> Unit)? = null,
    content: @Composable ColumnScope.(isExpanded: Boolean) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    
    val animatedIcon by animateFloatAsState(
        targetValue = if (expanded) 180f else 360f,
        animationSpec = tween(TRANSITION_ENTER_MS, easing = TRANSITION_EASING)
    )
    
    val animatedMaxHeight by animateDpAsState(
        label = "max_height",
        targetValue = if (expanded) {
            Float.POSITIVE_INFINITY.dp
        } else {
            maxUnexpandedHeight
        }
    )
    
    val onClick: () -> Unit = {
        expanded = !expanded
        onExpand?.invoke(expanded)
    }
    
    Card(
        modifier = modifier
            .wrapContentHeight(),
        onClick = {
            onClick()
        }
    ) {
        EdgeFadeWrapper(
            showStartEdgeFade = false,
            showEndEdgeFade = !expanded,
            orientation = Orientation.Vertical
        ) {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .heightIn(max = animatedMaxHeight)
                    .padding(top = 12.dp)
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
                            .padding(vertical = 4.dp),
                        text = title,
                        maxLines = if (expanded) 2 else 1
                    )
                    
                    ClickableIcon(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = animatedIcon
                            },
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = if (expanded) {
                            stringResource(Res.string.button_expand_less)
                        } else {
                            stringResource(Res.string.button_expand_more)
                        }
                    ) {
                        onClick()
                    }
                }
                
                HorizontalSeparator()
                
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    content(expanded)
                    
                    AnimatedVisibility(expanded) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    maxLines: Int = 2,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.titleLarge,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

@Composable
fun SubtitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    maxLines: Int = 2,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign
    )
}

@Composable
fun OptionDetailText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
    ) {
        SubtitleText(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        SmallText(
            text = subtitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun BodyText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        maxLines = maxLines
    )
}

@Composable
fun SmallText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
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
        maxLines = maxLines
    )
}

@Composable
fun HintText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 2
) {
    Text(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize(),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            autoCorrect = true,
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
        supportingText = {
            if (maxTextLength != null) {
                Text("${noteText.length}/$maxTextLength")
            }
        }
    )
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
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
            modifier = modifier
                .fillMaxWidth(),
            enabled = enabled,
            onClick = onClick
        ) {
            buttonText()
        }
    } else {
        Button(
            modifier = modifier
                .fillMaxWidth(),
            enabled = enabled,
            onClick = onClick
        ) {
            buttonText()
        }
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
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .consumeClick()
            .padding(paddingValues)
    ) {
        TextButton(
            text = text
        ) {
            onClick()
        }
    }
}

@Composable
fun ClickableIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
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

@Composable
fun ToggleableIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean,
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

suspend fun showSnackBar(
    message: String,
    actionLabel: String,
    snackbarHost: SnackbarHostState,
    onVibrate: () -> Unit,
    onComplete: (() -> Unit)? = null
) {
    // clear any existing snackbars
    snackbarHost.currentSnackbarData?.dismiss()
    
    onVibrate()
    
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

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                onEnabled(!enabled)
            }
            .padding(end = 16.dp)
    ) {
        OptionDetailText(
            modifier = Modifier.weight(1f),
            title = title,
            subtitle = subtitle
        )
        
        Switch(
            checked = enabled,
            onCheckedChange = {
                onEnabled(it)
            }
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
                    ),
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
fun HorizontalSeparator() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
    )
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
    @FloatRange(from = 0.0, to = 1.0) private val progress: Float,
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

@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= getTodayUtcMs()
    override fun isSelectableYear(year: Int): Boolean = year <= getTodayDate().year
}