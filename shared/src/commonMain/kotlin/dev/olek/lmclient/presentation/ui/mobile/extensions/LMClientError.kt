package dev.olek.lmclient.presentation.ui.mobile.extensions

import androidx.compose.runtime.Composable
import dev.olek.lmclient.data.models.LMClientError
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.error_message_api_key
import lm_client.shared.generated.resources.error_message_internet
import lm_client.shared.generated.resources.error_message_permission
import lm_client.shared.generated.resources.error_message_rate_limit
import lm_client.shared.generated.resources.error_message_timeout
import org.jetbrains.compose.resources.stringResource

@Composable
fun LMClientError.toLocalizedMessage(): String = when (this) {
    LMClientError.Authentication -> stringResource(Res.string.error_message_api_key)
    LMClientError.ConnectionIssue -> stringResource(Res.string.error_message_internet)
    LMClientError.PermissionDenied -> stringResource(Res.string.error_message_permission)
    LMClientError.RateLimit -> stringResource(Res.string.error_message_rate_limit)
    LMClientError.Timeout -> stringResource(Res.string.error_message_timeout)
    is LMClientError.UnknownError -> this.message
}
