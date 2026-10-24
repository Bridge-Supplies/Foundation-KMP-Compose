package ui.home

import bridge.supplies.foundation.ui.components.PastOrPresentSelectableDates
import bridge.supplies.foundation.ui.sheets.DateSheet
import bridge.supplies.foundation.ui.sheets.sheetController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.ui.foundationViewModel
import bridge.supplies.foundation.ui.isPortrait
import data.MainViewModel
import foundation.composeapp.generated.resources.Res
import foundation.composeapp.generated.resources.app_about_orientation
import foundation.composeapp.generated.resources.app_about_platform
import foundation.composeapp.generated.resources.app_name
import foundation.composeapp.generated.resources.ic_launcher_foreground
import foundation.composeapp.generated.resources.navigation_home_columns
import foundation.composeapp.generated.resources.navigation_home_date
import foundation.composeapp.generated.resources.navigation_home_grids
import foundation.composeapp.generated.resources.navigation_home_rows
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import bridge.supplies.foundation.core.time.getTodayUtcMs
import bridge.supplies.foundation.ui.components.EdgeFadeColumn
import bridge.supplies.foundation.ui.components.FloatingButton
import bridge.supplies.foundation.ui.components.TitleText

@Composable
fun HomeInfoScreen(
    viewModel: MainViewModel,
    hapticFeedback: () -> Unit,
    onNavigateDateScreen: (selectedDate: Long) -> Unit,
    onNavigateColumnsScreen: () -> Unit,
    onNavigateRowsScreen: () -> Unit,
    onNavigateGridsScreen: () -> Unit
) {
    val sheets = sheetController()
    val isPortraitMode = isPortrait()
    val listState = rememberScrollState()
    val orientationText = if (isPortraitMode) "Portrait" else "Landscape"
    val text = stringResource(Res.string.app_about_platform, foundationViewModel().platform.name) + "\n" +
        stringResource(Res.string.app_about_orientation, orientationText)
    
    EdgeFadeColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = listState,
        endSpacing = 16.dp
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_launcher_foreground),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            contentDescription = stringResource(Res.string.app_name),
            modifier = Modifier
                .height(128.dp)
                .fillMaxWidth()
                .padding(8.dp)
        )
        
        TitleText(
            textAlign = TextAlign.Center,
            text = text,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.weight(1f))
        
        FloatingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.navigation_home_date)
        ) {
            sheets.show(
                DateSheet(
                    initialDateMs = getTodayUtcMs(),
                    selectableDates = PastOrPresentSelectableDates,
                    onSelected = { selectedDate ->
                        onNavigateDateScreen(selectedDate)
                    }
                )
            )
            hapticFeedback()
        }
        
        FloatingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.navigation_home_columns)
        ) {
            onNavigateColumnsScreen()
            hapticFeedback()
        }
        
        FloatingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.navigation_home_rows)
        ) {
            onNavigateRowsScreen()
            hapticFeedback()
        }
        
        FloatingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.navigation_home_grids)
        ) {
            onNavigateGridsScreen()
            hapticFeedback()
        }
    }
}