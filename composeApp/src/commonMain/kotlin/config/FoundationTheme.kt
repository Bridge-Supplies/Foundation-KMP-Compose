package config

import androidx.compose.runtime.Composable
import data.MainViewModel

@Composable
expect fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
)