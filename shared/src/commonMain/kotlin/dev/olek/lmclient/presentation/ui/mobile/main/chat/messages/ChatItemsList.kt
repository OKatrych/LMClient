@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.main.chat.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListComponent
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem
import dev.olek.lmclient.presentation.ui.mobile.common.popup.ErrorPopup
import dev.olek.lmclient.presentation.util.clipEntryOf
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.error_popup_title_general
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatItemsList(
    listState: LazyListState,
    component: ChatItemsListComponent,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()

    if (state.chatItems.isEmpty() && !state.isGeneratingResponse) {
        EmptyChatItem(
            modifier = modifier
                .padding(paddingValues),
            isProviderConfigured = state.isProviderConfigured,
        )
    } else {
        ChatItemsListContent(
            modifier = modifier,
            listState = listState,
            items = state.chatItems,
            isGeneratingResponse = state.isGeneratingResponse,
            paddingValues = paddingValues,
            onCopy = component::copy,
            onRetry = component::retry,
        )
    }

    LaunchedEffect(state.isGeneratingResponse) {
        if (state.isGeneratingResponse) {
            // Scroll to the top when generating a new response
            listState.animateScrollToItem(0)
        }
    }

    Events(component)
}

@Composable
private fun ChatItemsListContent(
    listState: LazyListState,
    items: List<ChatItem>,
    isGeneratingResponse: Boolean,
    paddingValues: PaddingValues,
    onCopy: (ChatItem.AssistantItem) -> Unit,
    onRetry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = paddingValues,
    ) {
        if (isGeneratingResponse) {
            item(key = "loading") {
                PromptGeneratingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp),
                )
            }
        }
        items.fastForEach { item ->
            when (item) {
                is ChatItem.UserItem -> userChatItem(item)
                is ChatItem.AssistantItem -> assistantChatItem(
                    item = item,
                    onCopy = { onCopy(item) },
                    onRetry = { onRetry(item.id) },
                    onShare = {  },
                )
            }
        }
    }
}

@Composable
private fun Events(component: ChatItemsListComponent) {
    val clipboard = LocalClipboard.current
    var errorEvent by remember { mutableStateOf<LMClientError?>(null) }

    LaunchedEffect(component, clipboard) {
        component.event.collect { event ->
            when (event) {
                is ChatItemsListComponent.Event.CopyText -> {
                    clipboard.setClipEntry(clipEntryOf(event.text))
                }
                is ChatItemsListComponent.Event.Error -> errorEvent = event.error
            }
        }
    }

    errorEvent?.let { error ->
        ErrorPopup(
            title = stringResource(Res.string.error_popup_title_general),
            error = error,
            onDismissRequest = { errorEvent = null },
        )
    }
}
