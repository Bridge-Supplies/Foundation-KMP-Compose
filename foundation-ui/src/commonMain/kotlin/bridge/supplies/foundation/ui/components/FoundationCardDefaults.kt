package bridge.supplies.foundation.ui.components

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** The one card fill every Foundation card shares: `surfaceContainer`, whether flat or elevated. */
internal object FoundationCardDefaults {
    val colors: CardColors
        @Composable
        get() = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    val elevatedColors: CardColors
        @Composable
        get() =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
}
