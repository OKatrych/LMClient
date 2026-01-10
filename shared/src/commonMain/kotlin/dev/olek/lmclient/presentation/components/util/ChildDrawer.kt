package dev.olek.lmclient.presentation.components.util

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.NavigationSource
import com.arkivanov.decompose.router.children.SimpleChildNavState
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value

fun <T : Any> ComponentContext.childDrawer(
    source: NavigationSource<Boolean>,
    key: String = "drawer",
    childFactory: (ComponentContext) -> T,
): Value<ChildDrawer<T>> = children(
    source = source,
    key = key,
    initialState = { DrawerNavState(isOpen = false) },
    saveState = { null },
    restoreState = { null },
    navTransformer = { state, event -> state.copy(isOpen = event) },
    stateMapper = { state, children ->
        ChildDrawer(
            instance = requireNotNull(children.single().instance),
            isOpen = state.isOpen,
        )
    },
    backTransformer = { state ->
        if (state.isOpen) {
            { state.copy(isOpen = false) }
        } else {
            null
        }
    },
    childFactory = { _, componentContext -> childFactory(componentContext) },
)

data class ChildDrawer<out T : Any>(val instance: T, val isOpen: Boolean)

private data class DrawerNavState(val isOpen: Boolean) : NavState<Unit> {
    override val children: List<ChildNavState<Unit>> =
        listOf(
            SimpleChildNavState(
                configuration = Unit,
                status = if (isOpen) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED,
            ),
        )
}
