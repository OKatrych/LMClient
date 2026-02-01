package dev.olek.lmclient.presentation.components.main.chatitems

import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.data.models.MessageFinishReason
import com.mikepenz.markdown.model.State as MState

data class ChatItemsListState(
    val chatItems: List<ChatItem> = emptyList(),
    val chatRoom: ChatRoom? = null,
    val isGeneratingResponse: Boolean = false,
    val isProviderConfigured: Boolean = true,
) {
    sealed interface ChatItem {
        val id: String

        data class UserItem(override val id: String, val content: ChatItemContent) : ChatItem

        data class AssistantItem(
            override val id: String,
            val content: ChatItemContent,
            val finishReason: MessageFinishReason? = null,
            val error: LMClientError? = null,
        ) : ChatItem

        sealed interface ChatItemContent {
            data class TextContent(val text: String) : ChatItemContent

            data class MarkdownContent(val markdownState: MState.Success) : ChatItemContent

            data class ImageContent(val image: Any) : ChatItemContent // FIXME: add image support

            data class AudioContent(val audio: Any) : ChatItemContent // FIXME: add audio support
        }
    }
}
