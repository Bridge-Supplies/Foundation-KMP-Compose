import androidx.compose.runtime.Composable

@Composable
expect fun FoundationTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
)