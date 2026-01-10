package dev.olek.lmclient.presentation.ui.mobile.main.chat.messages

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.empty_chat
import lm_client.shared.generated.resources.empty_chat_no_provider
import lm_client.shared.generated.resources.ic_app_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyChatItem(
    isProviderConfigured: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .offset(y = -(maxHeight * 0.1f))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp),
                painter = painterResource(Res.drawable.ic_app_logo),
                tint = AppTheme.colors.icon,
                contentDescription = null,
            )
            Text(
                text = stringResource(
                    if (isProviderConfigured) {
                        Res.string.empty_chat
                    } else {
                        Res.string.empty_chat_no_provider
                    }
                ),
                style = AppTheme.typography.titleLargeMedium,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
