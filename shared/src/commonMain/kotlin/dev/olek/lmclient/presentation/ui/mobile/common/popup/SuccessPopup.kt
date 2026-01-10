@file:OptIn(ExperimentalMaterial3Api::class)

package dev.olek.lmclient.presentation.ui.mobile.common.popup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.olek.lmclient.presentation.theme.AppTheme
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_success
import lm_client.shared.generated.resources.popup_cta
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SuccessPopup(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    AppPopup(
        modifier = modifier,
        title = title,
        onDismissRequest = onDismissRequest,
        dismissButtonText = stringResource(Res.string.popup_cta),
        icon = {
            Icon(
                painter = painterResource(Res.drawable.ic_success),
                tint = AppTheme.colors.success,
                contentDescription = null,
            )
        },
    ) {
        if (message != null) {
            Text(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(top = 24.dp, start = 32.dp, end = 32.dp),
                text = message,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun SuccessPopupPreviewLight() {
    AppTheme(darkTheme = false) {
        SuccessPopup(
            title = "Connection successful",
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun SuccessPopupPreviewDark() {
    AppTheme(darkTheme = true) {
        SuccessPopup(
            title = "Connection successful",
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun SuccessPopupWithMessagePreview() {
    AppTheme(darkTheme = true) {
        SuccessPopup(
            title = "Connection successful",
            message = "You can now continue and use the chat",
            onDismissRequest = {},
        )
    }
}
