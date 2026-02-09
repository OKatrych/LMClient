@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.olek.lmclient.presentation.components.main

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import dev.olek.lmclient.data.models.ChatRoom
import dev.olek.lmclient.data.models.Message
import dev.olek.lmclient.data.models.MessageAttachment
import dev.olek.lmclient.data.models.MessageContent
import dev.olek.lmclient.data.models.Model
import dev.olek.lmclient.data.repositories.AttachmentsRepository
import dev.olek.lmclient.data.repositories.ChatMessagesRepository
import dev.olek.lmclient.data.repositories.ChatRoomRepository
import dev.olek.lmclient.data.repositories.ModelProviderRepository
import dev.olek.lmclient.data.repositories.observeActiveProvider
import dev.olek.lmclient.data.util.combine
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface QueryInputComponent {
    val state: StateFlow<State>

    fun onQueryChange(query: String)

    fun onQuerySubmit()

    fun onQueryCancel()

    fun onAddAttachment(file: PlatformFile)

    fun onRemoveAttachment(attachment: MessageAttachment)

    /**
     * @param isEnabled whether component is shown.
     * @param query the query text.
     * @param isLoading whether the system message is being generated.
     */
    data class State(
        val isEnabled: Boolean = false,
        val query: String = "",
        val attachmentsState: AttachmentsState = AttachmentsState(),
        val isLoading: Boolean = false,
    ) {
        /**
         * @param attachments the list of attachments.
         * @param canAttachImages whether the current model supports image attachments.
         * @param canAttachDocuments whether the current model supports document attachments.
         * @param supportedExtensions full list of supported model attachment extensions.
         */
        data class AttachmentsState(
            val attachments: List<MessageAttachment> = emptyList(),
            val canAttachImages: Boolean = false,
            val canAttachDocuments: Boolean = false,
            val supportedExtensions: List<String> = emptyList(),
        )
    }
}

internal class QueryInputComponentImpl(
    context: ComponentContext
) : QueryInputComponent, KoinComponent, ComponentContext by context {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    private val logger = Logger.withTag("QueryInputComponent")
    private val chatRoomRepository: ChatRoomRepository by inject()
    private val chatMessagesRepository: ChatMessagesRepository by inject()
    private val modelProviderRepository: ModelProviderRepository by inject()
    private val attachmentsRepository: AttachmentsRepository by inject()

    private val queryState: MutableStateFlow<String> = MutableStateFlow("")
    private val attachmentsState: MutableStateFlow<List<MessageAttachment>> =
        MutableStateFlow(emptyList())

    private val chatRoomFlow: StateFlow<ChatRoom?> = chatRoomRepository
        .observeActiveChatRoom()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val messageGenerationFlow = chatRoomFlow.flatMapLatest { chatRoom ->
        if (chatRoom != null) {
            chatMessagesRepository.observeMessageGenerationStatus(chatRoom.id)
        } else {
            flowOf(false)
        }
    }

    private val modelProviderFlow = modelProviderRepository
        .observeActiveProvider()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val activeModelFlow = modelProviderRepository
        .observeActiveModel()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    override val state: StateFlow<QueryInputComponent.State> = combine(
        queryState,
        attachmentsState,
        modelProviderFlow,
        activeModelFlow,
        messageGenerationFlow,
        chatRoomFlow,
    ) { query, attachments, activeProvider, activeModel, isMessageGenerating, activeChatRoom ->
        val hasSameProvider =
            activeChatRoom == null || activeProvider?.id == activeChatRoom.modelProviderId
        val imageCapability = activeModel?.capabilities
            ?.filterIsInstance<Model.Capability.Vision.Image>()
            ?.firstOrNull()
        val documentCapability = activeModel?.capabilities
            ?.filterIsInstance<Model.Capability.Document>()
            ?.firstOrNull()
        val extensions = buildList {
            imageCapability?.let { addAll(it.fileExtensions) }
            documentCapability?.let { addAll(it.fileExtensions) }
        }

        QueryInputComponent.State(
            query = query,
            attachmentsState = QueryInputComponent.State.AttachmentsState(
                attachments = attachments,
                canAttachImages = imageCapability != null,
                canAttachDocuments = documentCapability != null,
                supportedExtensions = extensions,
            ),
            isLoading = isMessageGenerating,
            isEnabled = activeProvider != null && activeModel != null && hasSameProvider,
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = QueryInputComponent.State(),
    )

    override fun onQueryChange(query: String) {
        queryState.update { query }
    }

    override fun onQuerySubmit() {
        coroutineScope.launch {
            logger.d { "onQuerySubmit" }
            val currentState = state.value
            val userMessage = Message.UserMessage(
                content = MessageContent.Text(currentState.query),
                attachments = currentState.attachmentsState.attachments,
            )
            val chatRoom = getOrCreateChatRoom(userMessage)

            chatMessagesRepository.generateMessage(
                prompt = userMessage,
                chatRoom = chatRoom,
            )
            queryState.update { "" }
            attachmentsState.update { emptyList() }
        }
    }

    override fun onQueryCancel() {
        logger.d { "onQueryCancel" }
        chatMessagesRepository.cancelMessageGeneration(
            chatRoomId = chatRoomFlow.value?.id ?: error("Chat room should not be null"),
        )
    }

    override fun onAddAttachment(file: PlatformFile) {
        coroutineScope.launch {
            logger.d { "onAddAttachment: ${file.name}" }
            val attachment = attachmentsRepository.processUserAttachment(file)
            attachmentsState.update { it + attachment }
        }
    }

    override fun onRemoveAttachment(attachment: MessageAttachment) {
        coroutineScope.launch {
            logger.d { "onRemoveAttachment: $attachment" }
            attachmentsRepository.deleteAttachment(attachment.content)
            attachmentsState.update { it - attachment }
        }
    }

    private suspend fun getOrCreateChatRoom(userMessage: Message.UserMessage): ChatRoom =
        chatRoomFlow.value ?: run {
            val name = when (userMessage.content) {
                is MessageContent.Audio -> "Audio conversation"
                is MessageContent.Text -> userMessage.content.text.take(15)
            }
            val providerId = modelProviderFlow.value?.id
                ?: error("Model provider should not be null")
            val modelId = activeModelFlow.value?.id
                ?: error("Model should not be null")
            chatRoomRepository
                .createChatRoom(
                    modelProviderId = providerId,
                    name = name,
                    modelId = modelId,
                ).also {
                    chatRoomRepository.setActiveChatRoom(it.id)
                }
        }
}

class QueryInputComponentPreview(
    customState: QueryInputComponent.State = QueryInputComponent.State(isEnabled = true),
) : QueryInputComponent {
    override val state: StateFlow<QueryInputComponent.State> = MutableStateFlow(customState)

    override fun onQueryChange(query: String) = Unit

    override fun onQuerySubmit() = Unit

    override fun onQueryCancel() = Unit

    override fun onAddAttachment(file: PlatformFile) = Unit

    override fun onRemoveAttachment(attachment: MessageAttachment) = Unit
}
