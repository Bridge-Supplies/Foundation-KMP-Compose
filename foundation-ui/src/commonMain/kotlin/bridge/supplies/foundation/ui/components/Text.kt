package bridge.supplies.foundation.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
public fun TitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier.wrapContentHeight().animateContentSize(),
        text = text,
        style = MaterialTheme.typography.titleLarge,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor,
    )
}

@Composable
public fun SubtitleText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier.wrapContentHeight().animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor,
    )
}

@Composable
public fun OptionDetailText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    titleMaxLines: Int = 1,
    subtitleMaxLines: Int = 3,
    textColor: Color = Color.Unspecified,
) {
    Column(modifier = modifier) {
        SubtitleText(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            maxLines = titleMaxLines,
            textColor = textColor,
        )

        if (subtitle != null) {
            SmallText(
                text = subtitle,
                modifier = Modifier.fillMaxWidth(),
                maxLines = subtitleMaxLines,
                textColor = textColor,
            )
        }
    }
}

@Composable
public fun BodyText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier.wrapContentHeight().animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis,
        color = textColor,
        textAlign = textAlign,
        minLines = minLines,
        maxLines = maxLines,
    )
}

@Composable
public fun SmallText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Unspecified,
    minLines: Int = 1,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier.wrapContentHeight().animateContentSize(),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = textColor,
        minLines = minLines,
        maxLines = maxLines,
    )
}

@Composable
public fun HintText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    minLines: Int = 1,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier.wrapContentHeight().animateContentSize(),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
    )
}
