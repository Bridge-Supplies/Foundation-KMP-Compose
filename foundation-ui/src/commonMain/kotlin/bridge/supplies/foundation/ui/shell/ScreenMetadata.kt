package bridge.supplies.foundation.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

/** How much shell UI a screen shows. */
public sealed interface Chrome {
    /** Top bar, plus bottom bar or rail. */
    public data object Standard : Chrome

    /** No top bar, bottom bar or rail, e.g. a game board. Optionally hides the system bars too. */
    public data class Immersive(val hideSystemBars: Boolean = false) : Chrome
}

private const val KEY_TITLE = "bridge.supplies.foundation.title"
private const val KEY_TITLE_TEXT = "bridge.supplies.foundation.titleText"
private const val KEY_CHROME = "bridge.supplies.foundation.chrome"

/**
 * Metadata for an entry, e.g. `entry<DetailRoute>(metadata = screenMetadata(title = Res.string.detail)) { … }`.
 * Without a title, the top bar shows the tab's title.
 */
public fun screenMetadata(
    title: StringResource? = null,
    titleText: String? = null,
    chrome: Chrome = Chrome.Standard,
): Map<String, Any> = buildMap {
    title?.let { put(KEY_TITLE, it) }
    titleText?.let { put(KEY_TITLE_TEXT, it) }
    put(KEY_CHROME, chrome)
}

internal val Map<String, Any>.screenTitle: StringResource? get() = get(KEY_TITLE) as? StringResource
internal val Map<String, Any>.screenTitleText: String? get() = get(KEY_TITLE_TEXT) as? String
internal val Map<String, Any>.screenChrome: Chrome get() = get(KEY_CHROME) as? Chrome ?: Chrome.Standard


// Top bar actions

/** A top bar icon button. Declare it from a screen with [TopBarActions]. */
public class TopBarAction(
    public val icon: ImageVector,
    public val label: StringResource,
    public val onClick: () -> Unit,
)

/** Shows [actions] in the top bar while the calling screen is the current one. */
@Composable
public fun TopBarActions(actions: List<TopBarAction>) {
    val host = LocalTopBarActionHost.current
    val route = LocalRoute.current
    SideEffect { host.actions[route] = actions }
    DisposableEffect(host, route) {
        onDispose { host.actions.remove(route) }
    }
}

internal class TopBarActionHost {
    val actions = mutableStateMapOf<NavKey, List<TopBarAction>>()
}

internal val LocalTopBarActionHost = staticCompositionLocalOf { TopBarActionHost() }

/** The route of the entry being composed. */
public val LocalRoute: ProvidableCompositionLocal<NavKey> =
    compositionLocalOf { error("LocalRoute is only available inside a FoundationApp entry") }
