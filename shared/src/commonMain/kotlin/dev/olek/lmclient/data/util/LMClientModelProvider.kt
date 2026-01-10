package dev.olek.lmclient.data.util

import kotlinx.serialization.Serializable

/**
 * List of supported model providers. If you want to add more, don't forget
 * to add a database migration.
 * @see dev.olek.lmclient.data.databases.ModelProviders.sq file.
 */
@Serializable
sealed class LMClientModelProvider(val id: String, val display: String) {
    @Serializable
    data object Google : LMClientModelProvider("google", "Google")

    @Serializable
    data object OpenAI : LMClientModelProvider("openai", "OpenAI")

    @Serializable
    data object Claude : LMClientModelProvider("claude", "Claude")

    @Serializable
    data object OpenRouter : LMClientModelProvider("openrouter", "OpenRouter")

    @Serializable
    data object Ollama : LMClientModelProvider("ollama", "Ollama")

    @Serializable
    data object DeepSeek : LMClientModelProvider("deepseek", "DeepSeek")

    @Serializable
    data object NexosAI : LMClientModelProvider("nexos_ai", "Nexos.ai")

    @Serializable
    data object GithubCopilot : LMClientModelProvider("github_copilot", "Github Copilot")

    companion object {
        fun fromId(id: String): LMClientModelProvider = when (id) {
            Google.id -> Google
            OpenAI.id -> OpenAI
            Claude.id -> Claude
            OpenRouter.id -> OpenRouter
            Ollama.id -> Ollama
            DeepSeek.id -> DeepSeek
            NexosAI.id -> NexosAI
            GithubCopilot.id -> GithubCopilot
            else -> error("Unsupported model provider: $id")
        }
    }
}
