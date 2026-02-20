@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.main.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.presentation.components.main.ChatHistoryComponent
import dev.olek.lmclient.presentation.components.main.ChatHistoryComponent.State
import dev.olek.lmclient.presentation.components.main.ChatHistoryComponentPreview
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreview
import dev.olek.lmclient.presentation.ui.mobile.common.ScreenPreviewWrapper
import dev.olek.lmclient.presentation.ui.mobile.common.SearchField
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.conversation_history_empty
import lm_client.shared.generated.resources.ic_settings_gear
import lm_client.shared.generated.resources.settings_button_content_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatHistoryScreen(
    component: ChatHistoryComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsStateMultiplatform()
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    val listTopPaddingPx = LocalDensity.current.run { ChatHistoryListPadding.toPx() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .lazyListHazeEffect(
                        hazeState = hazeState,
                        hazeStyle = HazeMaterials.thin(AppTheme.colors.background),
                        listState = listState,
                        listTopPaddingPx = listTopPaddingPx,
                    ).windowInsetsPadding(TopAppBarDefaults.windowInsets),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchField(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    onValueChange = component::onSearchQueryChange,
                )
                IconButton(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .padding(end = 8.dp),
                    onClick = component::navigateToSettings,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_settings_gear),
                        tint = AppTheme.colors.icon,
                        contentDescription = stringResource(Res.string.settings_button_content_desc),
                    )
                }
            }
        },
    ) { paddingValues ->
        HistoryList(
            modifier = Modifier
                .hazeSource(state = hazeState),
            chatRooms = state.chatRooms,
            onChatRoomClick = component::navigateToChatRoom,
            listState = listState,
            paddingValues = paddingValues + PaddingValues(vertical = ChatHistoryListPadding),
        )
    }
}

@Composable
private fun HistoryList(
    chatRooms: List<ChatRoom>,
    onChatRoomClick: (ChatRoom) -> Unit,
    paddingValues: PaddingValues,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (chatRooms.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.conversation_history_empty),
                style = AppTheme.typography.body,
                color = AppTheme.colors.text,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = paddingValues,
        ) {
            items(items = chatRooms, key = { it.id }) { chatRoom ->
                HistoryItem(chatRoom, onChatRoomClick)
            }
        }
    }
}

@Composable
private fun HistoryItem(
    chatRoom: ChatRoom,
    onClick: (ChatRoom) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(48.dp)
            .clickable {
                onClick(chatRoom)
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = chatRoom.name,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text,
        )
    }
}

@ScreenPreview
@Composable
private fun ChatHistoryScreenPreview() = ScreenPreviewWrapper {
    ChatHistoryScreen(
        ChatHistoryComponentPreview(
            State(
                chatRooms = listOf(
                    ChatRoom(
                        id = "id1",
                        name = "Conversation 1",
                        modelProviderId = "model_provider",
                        modelId = "1",
                    ),
                    ChatRoom(
                        id = "id2",
                        name = "Conversation 2",
                        modelProviderId = "model_provider",
                        modelId = "1",
                    ),
                    ChatRoom(
                        id = "id3",
                        name = "Conversation 3",
                        modelProviderId = "model_provider",
                        modelId = "3",
                    ),
                    ChatRoom(
                        id = "id4",
                        name = "Conversation 4",
                        modelProviderId = "model_provider",
                        modelId = "4",
                    ),
                ),
            ),
        ),
    )
}

private val ChatHistoryListPadding = 16.dp
