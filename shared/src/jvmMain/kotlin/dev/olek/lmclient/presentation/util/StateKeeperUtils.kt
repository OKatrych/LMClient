package dev.olek.lmclient.presentation.util

import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

/**
 * https://github.com/arkivanov/Decompose/blob/master/sample/shared/shared/src/jvmMain/kotlin/com/arkivanov/sample/shared/StateKeeperUtils.kt
 */
@OptIn(ExperimentalSerializationApi::class)
fun File.readSerializableContainer(): SerializableContainer? =
    takeIf(File::exists)?.inputStream()?.use { input ->
        try {
            Json.decodeFromStream(SerializableContainer.serializer(), input)
        } catch (e: SerializationException) {
            // Handle JSON deserialization errors - return null for graceful fallback
            println("StateKeeper: Failed to deserialize container: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            // Handle invalid input format - return null for graceful fallback
            println("StateKeeper: Invalid container format: ${e.message}")
            null
        }
    }
