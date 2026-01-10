package dev.olek.lmclient.presentation.ui.mobile.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import dev.olek.lmclient.data.models.ModelProvider
import dev.olek.lmclient.data.util.LMClientModelProvider
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_claude
import lm_client.shared.generated.resources.ic_deepseek
import lm_client.shared.generated.resources.ic_gemini
import lm_client.shared.generated.resources.ic_github
import lm_client.shared.generated.resources.ic_model_provider
import lm_client.shared.generated.resources.ic_nexos
import lm_client.shared.generated.resources.ic_ollama
import lm_client.shared.generated.resources.ic_openai
import lm_client.shared.generated.resources.ic_openrouter
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun ModelProvider.getIconRes(): DrawableResource = when (this.id) {
    LMClientModelProvider.OpenAI.id -> Res.drawable.ic_openai
    LMClientModelProvider.GithubCopilot.id -> Res.drawable.ic_github
    LMClientModelProvider.Google.id -> Res.drawable.ic_gemini
    LMClientModelProvider.Claude.id -> Res.drawable.ic_claude
    LMClientModelProvider.NexosAI.id -> Res.drawable.ic_nexos
    LMClientModelProvider.DeepSeek.id -> Res.drawable.ic_deepseek
    LMClientModelProvider.OpenRouter.id -> Res.drawable.ic_openrouter
    LMClientModelProvider.Ollama.id -> Res.drawable.ic_ollama
    else -> Res.drawable.ic_model_provider
}

@Composable
fun ModelProvider.getIconColorFilter(): ColorFilter? = when (this.id) {
    LMClientModelProvider.NexosAI.id -> null
    else -> ColorFilter.tint(AppTheme.colors.icon)
}
