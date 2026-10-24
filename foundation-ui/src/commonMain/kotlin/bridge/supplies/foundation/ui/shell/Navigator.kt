package bridge.supplies.foundation.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.StringResource

/** A top-level destination with its own back stack, shown in the bottom bar / rail. */
public data class FoundationTab(
    /** Stable id, used for saved state and [Navigator.navigate]. */
    public val id: String,
    public val title: StringResource,
    public val icon: ImageVector,
    public val selectedIcon: ImageVector,
    /** The tab's first screen. */
    public val root: NavKey,
)

/**
 * Registers every route type (`@Serializable` classes implementing [NavKey]) so back stacks can be saved
 * and restored on all platforms, e.g. `RouteRegistry { subclass(HomeRoute::class); subclass(DetailRoute::class) }`.
 */
public class RouteRegistry(registration: PolymorphicModuleBuilder<NavKey>.() -> Unit) {
    internal val configuration: SavedStateConfiguration = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) { registration() }
        }
    }
}

/** Navigation for screens inside [FoundationApp]. Get it with `LocalNavigator.current`. */
@Stable
public class Navigator internal constructor(
    public val tabs: List<FoundationTab>,
    private val stacks: Map<String, NavBackStack<NavKey>>,
    private val selectedTabId: MutableState<String>,
) {
    public val currentTabId: String get() = selectedTabId.value
    public val currentTab: FoundationTab get() = tabs.first { it.id == currentTabId }
    public val currentRoute: NavKey get() = currentStack.last()
    public val canNavigateBack: Boolean get() = currentStack.size > 1

    private val currentStack: NavBackStack<NavKey> get() = stackOf(currentTabId)
    internal fun stackOf(tabId: String): NavBackStack<NavKey> = stacks.getValue(tabId)

    /** Opens [route] on the current tab. */
    public fun navigate(route: NavKey) {
        currentStack.add(route)
    }

    /** Switches to [tabId] and opens [route] there, e.g. a cross-link from one tab into another. */
    public fun navigate(tabId: String, route: NavKey) {
        selectedTabId.value = tabId
        currentStack.add(route)
    }

    /** Selects [tabId]; selecting the current tab again returns it to its root. */
    public fun selectTab(tabId: String) {
        if (tabId == currentTabId) popToRoot() else selectedTabId.value = tabId
    }

    public fun popToRoot() {
        while (currentStack.size > 1) currentStack.removeAt(currentStack.lastIndex)
    }

    /** Goes back within the tab, or from another tab's root to the first tab. Returns false at the very start. */
    public fun back(): Boolean = when {
        canNavigateBack -> {
            currentStack.removeAt(currentStack.lastIndex)
            true
        }
        currentTabId != tabs.first().id -> {
            selectedTabId.value = tabs.first().id
            true
        }
        else -> false
    }

    /** Entry point for deep links and app shortcuts. */
    public fun openFromOutside(tabId: String, route: NavKey) {
        if (route == stackOf(tabId).first()) selectTab(tabId) else navigate(tabId, route)
    }
}

public val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("Navigator is only available inside FoundationApp") }

@Composable
internal fun rememberNavigator(tabs: List<FoundationTab>, routes: RouteRegistry): Navigator {
    val stacks = tabs.associate { tab -> tab.id to rememberNavBackStack(routes.configuration, tab.root) }
    val selectedTabId = rememberSaveable { mutableStateOf(tabs.first().id) }
    return remember(tabs) { Navigator(tabs, stacks, selectedTabId) }
}
