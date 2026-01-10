@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalStateKeeperApi::class)

package dev.olek.lmclient.presentation.components.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import dev.olek.lmclient.presentation.components.util.ChildDrawer
import dev.olek.lmclient.presentation.components.util.childDrawer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinComponent

interface MainScreenComponent {
    val historyDrawer: Value<ChildDrawer<ChatHistoryComponent>>
    val chatScreenComponent: ChatScreenComponent

    fun setDrawerState(isOpen: Boolean)
}

internal class MainScreenComponentImpl(
    context: ComponentContext,
    onSettingsClick: () -> Unit,
    onConfigureProviderClick: () -> Unit,
) : MainScreenComponent,
    KoinComponent,
    ComponentContext by context {
    private val drawerNavigation = SimpleNavigation<Boolean>()

    override val historyDrawer: Value<ChildDrawer<ChatHistoryComponent>> = childDrawer(
        source = drawerNavigation,
        childFactory = { context ->
            ChatHistoryComponentImpl(
                context = context,
                onChatRoomClicked = { setDrawerState(false) },
                onSettingsClicked = {
                    setDrawerState(false)
                    onSettingsClick()
                },
            )
        },
    )

    override val chatScreenComponent: ChatScreenComponent = ChatScreenComponentImpl(
        context = childContext("ChatScreenComponent"),
        onDrawerClick = { setDrawerState(true) },
        onConfigureProviderClick = onConfigureProviderClick,
    )

    override fun setDrawerState(isOpen: Boolean) {
        drawerNavigation.navigate(isOpen)
    }
}

data class MainScreenComponentPreview(
    val isHistoryDrawerOpen: Boolean = false,
    override val chatScreenComponent: ChatScreenComponent = ChatScreenComponentPreview(),
) : MainScreenComponent {
    override val historyDrawer: Value<ChildDrawer<ChatHistoryComponent>> = MutableValue(
        ChildDrawer(
            instance = ChatHistoryComponentPreview(ChatHistoryComponent.State()),
            isOpen = isHistoryDrawerOpen,
        ),
    )

    override fun setDrawerState(isOpen: Boolean) = Unit
}
