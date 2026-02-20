package dev.olek.lmclient.presentation.ui.mobile.main.chat.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem.ChatItemContent
import dev.olek.lmclient.presentation.theme.AppTheme

internal fun LazyListScope.userChatItem(item: ChatItem.UserItem) {
    item(key = item.id, contentType = "user") {
        UserChatItem(
            modifier = Modifier.padding(bottom = 16.dp),
            userItem = item,
        )
    }
}

@Composable
private fun UserChatItem(userItem: ChatItem.UserItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (userItem.attachments.isNotEmpty()) {
            // TODO add attachments lazy row
        }
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val itemWidth = maxWidth * 0.8f
            Box(
                modifier = Modifier
                    .background(color = AppTheme.colors.background, shape = AppTheme.shapes.card)
                    .widthIn(max = itemWidth)
                    .align(alignment = Alignment.CenterEnd),
            ) {
                when (userItem.content) {
                    is ChatItemContent.TextContent -> {
                        Text(
                            modifier = Modifier
                                .padding(8.dp),
                            text = userItem.content.text,
                            style = AppTheme.typography.body,
                            color = AppTheme.colors.textSecondary,
                        )
                    }

                    is ChatItemContent.AudioContent -> TODO()
                    is ChatItemContent.MarkdownContent -> error("Markdown should not be used here")
                }
            }
        }
    }
}
