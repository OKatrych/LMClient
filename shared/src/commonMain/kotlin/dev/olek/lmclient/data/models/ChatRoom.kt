package dev.olek.lmclient.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a chat room.
 * @param id - unique identifier of the chat room
 * @param name - title of the chat room
 * @param modelProviderId - model provider assigned to the chat room (cannot be changed)
 * @param modelId - model used to generate last message in the chat
 */
@Serializable
data class ChatRoom(val id: String, val name: String, val modelProviderId: String, val modelId: String?)
