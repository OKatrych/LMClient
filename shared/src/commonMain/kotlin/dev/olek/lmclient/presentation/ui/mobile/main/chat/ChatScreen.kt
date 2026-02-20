@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.main.chat

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.olek.lmclient.presentation.components.main.ChatScreenComponent
import dev.olek.lmclient.presentation.components.main.ChatScreenComponentPreview
import dev.olek.lmclient.presentation.components.main.ChatTopBarComponent
import dev.olek.lmclient.presentation.components.main.QueryInputComponent
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponent
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponentPreview
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.AppButton
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreview
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.ui.mobile.main.chat.messages.ChatItemsList
import dev.olek.lmclient.presentation.ui.mobile.main.chat.queryinput.QueryInputBox
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.configure_provider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatScreen(
    component: ChatScreenComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()
    ChatScreenContent(
        modifier = modifier,
        chatTopBarComponent = component.chatTopBarComponent,
        chatItemsListComponent = component.chatItemsListComponent,
        queryInputComponent = component.queryInputComponent,
        isProviderConfigured = state.isProviderConfigured,
        onConfigureProviderClick = component::navigateToProviderList,
    )
}

@Composable
private fun ChatScreenContent(
    chatTopBarComponent: ChatTopBarComponent,
    chatItemsListComponent: ChatItemsListComponent,
    queryInputComponent: QueryInputComponent,
    isProviderConfigured: Boolean,
    onConfigureProviderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        contentColor = Color.Unspecified,
        topBar = {
            ChatTopBar(
                component = chatTopBarComponent,
                modifier = Modifier
                    .onSizeChanged { topBarHeightPx = it.height.toFloat() }
                    .lazyListHazeEffect(
                        hazeState = hazeState,
                        hazeStyle = HazeMaterials.thin(AppTheme.colors.surface),
                        listState = listState,
                        // For reverse layout, need to account top bar height
                        listTopPaddingPx = topBarHeightPx,
                    ).fillMaxWidth(),
            )
        },
        bottomBar = {
            if (isProviderConfigured) {
                QueryInputBox(component = queryInputComponent)
            } else {
                ConfigureProviderButton(onClick = onConfigureProviderClick)
            }
        },
        floatingActionButton = {
            // TODO add FAB scroll to bottom
        },
        content = { paddingValues ->
            ChatItemsList(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                listState = listState,
                component = chatItemsListComponent,
                paddingValues = paddingValues + PaddingValues(MessagesListPadding),
            )
        },
    )
}

@Composable
private fun ConfigureProviderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppButton(
        modifier = modifier
            .padding(16.dp)
            .navigationBarsPadding(),
        text = stringResource(Res.string.configure_provider),
        onClick = onClick,
    )
}

@ScreenPreview
@Composable
private fun ChatScreenEmptyPreview() = ScreenPreviewWrapper {
    ChatScreen(
        component = ChatScreenComponentPreview(),
    )
}

@ScreenPreview
@Composable
private fun ChatScreenGeneratingPreview() = ScreenPreviewWrapper {
    ChatScreen(
        component = ChatScreenComponentPreview(
            chatItemsListComponent = ChatItemsListComponentPreview(
                customState = ChatItemsListState(
                    isGeneratingResponse = true,
                    chatItems = listOf(),
                ),
            ),
        ),
    )
}

private val MessagesListPadding = 16.dp
